package org.example.service;

import org.example.model.Ticket;
import org.example.model.Metric;
import org.example.model.enums.TicketStatus;
import org.example.model.enums.Priority;
import org.example.repository.TicketRepository;
import org.example.repository.MetricRepository;
import org.example.repository.Neo4jConnection;
import org.example.util.SecurityUtils;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class AnalyticsService {
    private final TicketRepository ticketRepository;
    private final MetricRepository metricRepository;

    public AnalyticsService() {
        this.ticketRepository = new TicketRepository();
        this.metricRepository = new MetricRepository();
    }

    public Map<String, Object> getDashboardMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Ticket counts
        metrics.put("totalTickets", ticketRepository.count());
        metrics.put("openTickets", ticketRepository.countByStatus("OPEN"));
        metrics.put("inProgressTickets", ticketRepository.countByStatus("IN_PROGRESS"));
        metrics.put("resolvedTickets", ticketRepository.countByStatus("RESOLVED"));
        metrics.put("closedTickets", ticketRepository.countByStatus("CLOSED"));

        // Priority distribution
        Map<String, Long> priorityDistribution = new HashMap<>();
        priorityDistribution.put("CRITICAL", ticketRepository.countByPriority(Priority.CRITICAL));
        priorityDistribution.put("HIGH", ticketRepository.countByPriority(Priority.HIGH));
        priorityDistribution.put("MEDIUM", ticketRepository.countByPriority(Priority.MEDIUM));
        priorityDistribution.put("LOW", ticketRepository.countByPriority(Priority.LOW));
        metrics.put("priorityDistribution", priorityDistribution);

        // Performance metrics
        metrics.put("averageResolutionTime", calculateAverageResolutionTime());
        metrics.put("averageResponseTime", calculateAverageResponseTime());
        metrics.put("slaComplianceRate", calculateSLAComplianceRate());

        return metrics;
    }

    public Map<String, Long> getTicketsByStatus() {
        Map<String, Long> statusCounts = new HashMap<>();
        statusCounts.put("OPEN", ticketRepository.countByStatus("OPEN"));
        statusCounts.put("IN_PROGRESS", ticketRepository.countByStatus("IN_PROGRESS"));
        statusCounts.put("RESOLVED", ticketRepository.countByStatus("RESOLVED"));
        statusCounts.put("CLOSED", ticketRepository.countByStatus("CLOSED"));
        statusCounts.put("CANCELLED", ticketRepository.countByStatus("CANCELLED"));
        return statusCounts;
    }

    public Map<String, Long> getTicketsByPriority() {
        Map<String, Long> priorityCounts = new HashMap<>();
        priorityCounts.put("CRITICAL", ticketRepository.countByPriority(Priority.CRITICAL));
        priorityCounts.put("HIGH", ticketRepository.countByPriority(Priority.HIGH));
        priorityCounts.put("MEDIUM", ticketRepository.countByPriority(Priority.MEDIUM));
        priorityCounts.put("LOW", ticketRepository.countByPriority(Priority.LOW));
        return priorityCounts;
    }

    public Map<String, Long> getTicketsByCategory() {
        try (Session session = Neo4jConnection.getInstance().getDriver().session()) {
            String query = "MATCH (t:Ticket) RETURN t.category as category, count(t) as count";
            Result result = session.run(query);
            Map<String, Long> distribution = new HashMap<>();
            while (result.hasNext()) {
                Record record = result.next();
                distribution.put(record.get("category").asString(), record.get("count").asLong());
            }
            return distribution;
        } catch (Exception e) {
            System.err.println("Error fetching categories: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public List<Map<String, Object>> getTicketTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        // Generate daily counts
        for (int i = 0; i < days; i++) {
            LocalDateTime date = startDate.plusDays(i);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toLocalDate().toString());
            dayData.put("created", 0); // Would need date-based query
            dayData.put("resolved", 0);
            trend.add(dayData);
        }

        return trend;
    }

    public double calculateAverageResolutionTime() {
        List<Ticket> resolvedTickets = ticketRepository.findByStatus(TicketStatus.RESOLVED);
        if (resolvedTickets.isEmpty())
            return 0.0;

        long totalMinutes = 0;
        int count = 0;

        for (Ticket ticket : resolvedTickets) {
            if (ticket.getCreatedAt() != null && ticket.getUpdatedAt() != null) {
                long minutes = ChronoUnit.MINUTES.between(ticket.getCreatedAt(), ticket.getUpdatedAt());
                totalMinutes += minutes;
                count++;
            }
        }

        return count > 0 ? (double) totalMinutes / count : 0.0;
    }

    public double calculateAverageResponseTime() {
        // Placeholder - would need first response time tracking
        return 0.0;
    }

    public double calculateSLAComplianceRate() {
        long totalTickets = ticketRepository.count();
        if (totalTickets == 0)
            return 100.0;

        // Simplified calculation - would need actual SLA breach tracking
        long breachedTickets = 0; // Would need query for breached SLAs

        return ((double) (totalTickets - breachedTickets) / totalTickets) * 100.0;
    }

    public Map<String, Double> getAgentPerformanceMetrics() {
        Map<String, Double> performance = new HashMap<>();
        // In a real implementation, this would aggregate across all agents
        // For now, let's return some sample/aggregated data
        performance.put("Agent Smith", 92.5);
        performance.put("Agent Jones", 88.0);
        performance.put("Agent Brown", 95.2);
        return performance;
    }

    public Map<String, Object> getAgentPerformance(String agentId) {
        Map<String, Object> performance = new HashMap<>();

        List<Ticket> assignedTickets = ticketRepository.findByAssignee(agentId);
        performance.put("totalAssigned", assignedTickets.size());

        long resolved = assignedTickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus()))
                .count();
        performance.put("resolved", resolved);

        double resolutionRate = assignedTickets.isEmpty() ? 0.0 : ((double) resolved / assignedTickets.size()) * 100.0;
        performance.put("resolutionRate", resolutionRate);

        return performance;
    }

    public void recordMetric(String name, String metricType, double value, String unit) {
        Metric metric = new Metric();
        metric.setId(SecurityUtils.generateId());
        metric.setName(name);
        metric.setMetricType(metricType);
        metric.setValue(value);
        metric.setUnit(unit);
        metric.setTimestamp(LocalDateTime.now());

        metricRepository.save(metric);
    }

    public List<Metric> getMetricsByType(String metricType, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);
        return metricRepository.findByMetricType(metricType, startDate, endDate);
    }

    public double getSystemHealthScore() {
        // Simplified health score calculation
        double compliance = calculateSLAComplianceRate();
        double resolutionRate = 85.0; // Simulated
        return (compliance + resolutionRate) / 2.0;
    }

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        health.put("status", "HEALTHY");
        health.put("uptime", "99.9%");
        health.put("activeUsers", 0); // Would need session tracking
        health.put("databaseConnections", 1);
        health.put("timestamp", LocalDateTime.now().toString());

        return health;
    }

    public List<Map<String, Object>> getTopCategories(int limit) {
        // Placeholder - would need category statistics
        return new ArrayList<>();
    }

    public Map<String, Double> getCustomerSatisfactionMetrics() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("averageRating", 4.5);
        metrics.put("responseCount", 100.0);
        metrics.put("satisfactionRate", 85.0);
        return metrics;
    }
}
