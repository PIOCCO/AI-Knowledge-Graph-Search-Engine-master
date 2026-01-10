package org.example.service;

import org.example.model.Category;
import org.example.model.Ticket;
import org.example.repository.CategoryRepository;
import org.example.repository.Neo4jConnection;

import java.util.List;
import java.util.stream.Collectors;

public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this.categoryRepository = new CategoryRepository();
    }

    /**
     * Get tickets for a category
     */
    public List<Ticket> getTicketsByCategory(String categoryId) {
        try (var session = Neo4jConnection.getInstance().getSession()) {
            String query =
                    "MATCH (c:Category {id: $categoryId})<-[:BELONGS_TO]-(t:Ticket) " +
                            "RETURN t ORDER BY t.createdAt DESC";

            var result = session.run(query,
                    org.neo4j.driver.Values.parameters("categoryId", categoryId));

            List<Ticket> tickets = new java.util.ArrayList<>();
            while (result.hasNext()) {
                var record = result.next();
                tickets.add(mapToTicket(record.get("t").asMap()));
            }

            return tickets;
        }
    }

    /**
     * Get category statistics
     */
    public java.util.Map<String, Object> getCategoryStats(String categoryId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        try (var session = Neo4jConnection.getInstance().getSession()) {
            // Total tickets
            String query =
                    "MATCH (c:Category {id: $categoryId})<-[:BELONGS_TO]-(t:Ticket) " +
                            "RETURN count(t) as total";

            var result = session.run(query,
                    org.neo4j.driver.Values.parameters("categoryId", categoryId));

            if (result.hasNext()) {
                stats.put("totalTickets", result.next().get("total").asLong());
            }

            // Auto-classified vs manual
            query =
                    "MATCH (c:Category {id: $categoryId})<-[r:BELONGS_TO]-(t:Ticket) " +
                            "RETURN r.autoClassified as autoClassified, count(t) as count";

            result = session.run(query,
                    org.neo4j.driver.Values.parameters("categoryId", categoryId));

            while (result.hasNext()) {
                var record = result.next();
                boolean auto = record.get("autoClassified").asBoolean(false);
                long count = record.get("count").asLong();
                stats.put(auto ? "autoClassified" : "manual", count);
            }

            // Average confidence
            query =
                    "MATCH (c:Category {id: $categoryId})<-[r:BELONGS_TO]-(t:Ticket) " +
                            "WHERE r.autoClassified = true " +
                            "RETURN avg(r.confidence) as avgConfidence";

            result = session.run(query,
                    org.neo4j.driver.Values.parameters("categoryId", categoryId));

            if (result.hasNext()) {
                stats.put("averageConfidence",
                        result.next().get("avgConfidence").asDouble(0.0));
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to get category stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Get related categories (categories that share tickets)
     */
    public List<Category> getRelatedCategories(String categoryId, int limit) {
        try (var session = Neo4jConnection.getInstance().getSession()) {
            // Find categories that share similar tickets based on keywords
            String query =
                    "MATCH (c1:Category {id: $categoryId})<-[:BELONGS_TO]-(t:Ticket) " +
                            "MATCH (t)-[:BELONGS_TO]->(c2:Category) " +
                            "WHERE c1 <> c2 " +
                            "RETURN c2, count(t) as sharedTickets " +
                            "ORDER BY sharedTickets DESC " +
                            "LIMIT $limit";

            var result = session.run(query,
                    org.neo4j.driver.Values.parameters(
                            "categoryId", categoryId,
                            "limit", limit
                    ));

            List<Category> categories = new java.util.ArrayList<>();
            while (result.hasNext()) {
                var record = result.next();
                categories.add(mapToCategory(record.get("c2").asMap()));
            }

            return categories;
        }
    }

    private Ticket mapToTicket(java.util.Map<String, Object> map) {
        Ticket ticket = new Ticket();
        ticket.setId((String) map.get("id"));
        ticket.setTitle((String) map.get("title"));
        ticket.setDescription((String) map.get("description"));
        ticket.setStatus((String) map.get("status"));
        ticket.setPriority((String) map.get("priority"));
        ticket.setCategory((String) map.get("category"));
        ticket.setAssignedTo((String) map.get("assignedTo"));
        ticket.setCreatedBy((String) map.get("createdBy"));
        return ticket;
    }

    private Category mapToCategory(java.util.Map<String, Object> map) {
        Category category = new Category();
        category.setId((String) map.get("id"));
        category.setName((String) map.get("name"));
        category.setDescription((String) map.get("description"));
        category.setColor((String) map.get("color"));
        return category;
    }
}
