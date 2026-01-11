package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.example.config.MLConfig;
import org.example.model.Ticket;
import org.example.model.ClassificationResult;
import org.example.repository.TicketRepository;
import org.example.repository.CategoryRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * ML Classification Service - Integrates with Python ML service
 * FIXED: Proper error handling and path resolution
 */
public class MLClassificationService {

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final MLConfig mlConfig;
    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final ExecutorService executorService;
    private final BlockingQueue<Ticket> retryQueue;

    // Service health check
    private volatile boolean serviceAvailable = false;

    public MLClassificationService() {
        this.mlConfig = MLConfig.getInstance();
        this.objectMapper = new ObjectMapper();
        this.ticketRepository = new TicketRepository();
        this.categoryRepository = new CategoryRepository();
        this.executorService = Executors.newFixedThreadPool(3);
        this.retryQueue = new LinkedBlockingQueue<>();

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(mlConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(mlConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(mlConfig.getTimeout(), TimeUnit.MILLISECONDS)
                .build();

        // Check service availability on startup
        checkServiceHealth();
        startRetryWorker();
    }

    /**
     * Check if ML service is available
     */
    private void checkServiceHealth() {
        try {
            Request request = new Request.Builder()
                    .url(mlConfig.getServiceUrl() + "/health")
                    .get()
                    .build();

            Response response = httpClient.newCall(request).execute();
            serviceAvailable = response.isSuccessful();
            response.close();

            if (serviceAvailable) {
                System.out.println("✅ ML Service is available at " + mlConfig.getServiceUrl());
            } else {
                System.err.println("⚠️ ML Service health check failed");
            }
        } catch (Exception e) {
            serviceAvailable = false;
            System.err.println("⚠️ ML Service not available: " + e.getMessage());
            System.err.println("💡 Make sure to start the Python ML service first:");
            System.err.println("   cd Ticket_Agent/ml_service && python ml_service.py");
        }
    }

    /**
     * Classify ticket asynchronously with fallback
     */
    public CompletableFuture<ClassificationResult> classifyTicketAsync(Ticket ticket) {
        if (!mlConfig.isEnabled()) {
            System.out.println("⚠️ ML Classification disabled - skipping");
            return CompletableFuture.completedFuture(null);
        }

        if (!serviceAvailable) {
            System.err.println("⚠️ ML Service unavailable - ticket will require manual classification");
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                return classifyTicket(ticket);
            } catch (Exception e) {
                System.err.println("❌ Classification failed for " + ticket.getId() + ": " + e.getMessage());
                retryQueue.offer(ticket);
                return null;
            }
        }, executorService);
    }

    /**
     * Classify ticket synchronously (blocking)
     */
    public ClassificationResult classifyTicket(Ticket ticket) throws IOException {
        System.out.println("🤖 Classifying ticket: " + ticket.getId());

        // Prepare request
        String requestJson = objectMapper.writeValueAsString(
                new ClassificationRequest(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getPriority()
                )
        );

        RequestBody body = RequestBody.create(
                requestJson,
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(mlConfig.getServiceUrl() + "/classify")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("ML Service returned: " + response.code());
            }

            String responseBody = response.body().string();
            ClassificationResult result = objectMapper.readValue(
                    responseBody,
                    ClassificationResult.class
            );

            // Apply classification if confidence meets threshold
            if (result.getConfidence() >= mlConfig.getConfidenceThreshold()) {
                applyClassification(ticket, result);
                System.out.println("✅ Classified as: " + result.getCategoryName() +
                        " (confidence: " + String.format("%.2f%%", result.getConfidence() * 100) + ")");
            } else {
                System.out.println("⚠️ Low confidence (" +
                        String.format("%.2f%%", result.getConfidence() * 100) +
                        ") - Manual classification required");
                ticket.setStatus("NEEDS_CLASSIFICATION");
                ticketRepository.update(ticket);
            }

            return result;

        } catch (IOException e) {
            System.err.println("❌ ML Service error: " + e.getMessage());
            serviceAvailable = false; // Mark as unavailable for future requests
            throw e;
        }
    }

    /**
     * Apply classification result to ticket
     */
    private void applyClassification(Ticket ticket, ClassificationResult result) {
        try {
            ticket.setCategoryId(result.getPredictedCategory());
            ticket.setCategory(result.getCategoryName());
            ticket.setMlPredictedCategory(result.getPredictedCategory());
            ticket.setMlConfidence(result.getConfidence());
            ticket.setMlClassifiedAt(LocalDateTime.now());
            ticket.setAutoClassified(true);

            ticketRepository.update(ticket);

            createCategoryRelationship(
                    ticket.getId(),
                    result.getPredictedCategory(),
                    result.getConfidence(),
                    true
            );

        } catch (Exception e) {
            System.err.println("❌ Failed to apply classification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create BELONGS_TO relationship in Neo4j
     */
    private void createCategoryRelationship(String ticketId, String categoryId,
                                            double confidence, boolean autoClassified) {
        try (var session = org.example.repository.Neo4jConnection.getInstance().getSession()) {
            String query =
                    "MATCH (t:Ticket {id: $ticketId}), (c:Category {id: $categoryId}) " +
                            "MERGE (t)-[r:BELONGS_TO]->(c) " +
                            "SET r.confidence = $confidence, " +
                            "    r.autoClassified = $autoClassified, " +
                            "    r.classifiedAt = datetime() " +
                            "RETURN r";

            session.run(query, org.neo4j.driver.Values.parameters(
                    "ticketId", ticketId,
                    "categoryId", categoryId,
                    "confidence", confidence,
                    "autoClassified", autoClassified
            ));

            System.out.println("✅ Created category relationship: " + ticketId + " -> " + categoryId);

        } catch (Exception e) {
            System.err.println("❌ Failed to create relationship: " + e.getMessage());
        }
    }

    /**
     * Manually override ML classification
     */
    public void overrideClassification(String ticketId, String newCategoryId) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId);
            if (ticket == null) {
                throw new IllegalArgumentException("Ticket not found: " + ticketId);
            }

            var category = categoryRepository.findById(newCategoryId);
            ticket.setCategoryId(newCategoryId);
            ticket.setCategory(category != null ? category.getName() : newCategoryId);
            ticketRepository.update(ticket);

            createCategoryRelationship(ticketId, newCategoryId, 1.0, false);

            System.out.println("✅ Manual classification override: " + ticketId + " -> " + newCategoryId);

        } catch (Exception e) {
            System.err.println("❌ Override failed: " + e.getMessage());
            throw new RuntimeException("Classification override failed", e);
        }
    }

    /**
     * Batch classify multiple tickets
     */
    public void batchClassify(java.util.List<Ticket> tickets) {
        System.out.println("🚀 Starting batch classification of " + tickets.size() + " tickets");

        java.util.List<CompletableFuture<ClassificationResult>> futures = tickets.stream()
                .map(this::classifyTicketAsync)
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long successful = futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result != null)
                .count();

        System.out.println("✅ Batch classification complete: " + successful + "/" + tickets.size() + " successful");
    }

    /**
     * Retry worker for failed classifications
     */
    private void startRetryWorker() {
        Thread retryWorker = new Thread(() -> {
            while (true) {
                try {
                    Ticket ticket = retryQueue.poll(10, TimeUnit.SECONDS);
                    if (ticket != null) {
                        System.out.println("🔄 Retrying classification for: " + ticket.getId());
                        Thread.sleep(5000);

                        // Re-check service availability
                        checkServiceHealth();

                        if (serviceAvailable) {
                            classifyTicket(ticket);
                        } else {
                            // Put back in queue if service still unavailable
                            retryQueue.offer(ticket);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Retry failed: " + e.getMessage());
                }
            }
        });
        retryWorker.setDaemon(true);
        retryWorker.start();
    }

    /**
     * Get classification statistics
     */
    public java.util.Map<String, Object> getStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        try (var session = org.example.repository.Neo4jConnection.getInstance().getSession()) {
            String query =
                    "MATCH (t:Ticket)-[r:BELONGS_TO]->(c:Category) " +
                            "WHERE r.autoClassified = true " +
                            "RETURN count(t) as autoClassified";

            var result = session.run(query);
            if (result.hasNext()) {
                stats.put("autoClassified", result.next().get("autoClassified").asLong());
            }

            query =
                    "MATCH (t:Ticket)-[r:BELONGS_TO]->(c:Category) " +
                            "WHERE r.autoClassified = false " +
                            "RETURN count(t) as manual";

            result = session.run(query);
            if (result.hasNext()) {
                stats.put("manualClassified", result.next().get("manual").asLong());
            }

            query =
                    "MATCH (t:Ticket)-[r:BELONGS_TO]->(c:Category) " +
                            "WHERE r.autoClassified = true " +
                            "RETURN avg(r.confidence) as avgConfidence";

            result = session.run(query);
            if (result.hasNext()) {
                stats.put("averageConfidence", result.next().get("avgConfidence").asDouble());
            }

            stats.put("pendingRetries", retryQueue.size());
            stats.put("serviceAvailable", serviceAvailable);

        } catch (Exception e) {
            System.err.println("❌ Failed to get statistics: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Shutdown service
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    // Inner class for classification request
    public static class ClassificationRequest {
        public String ticketId;
        public String title;
        public String description;
        public String priority;

        public ClassificationRequest(String ticketId, String title, String description, String priority) {
            this.ticketId = ticketId;
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
    }
}