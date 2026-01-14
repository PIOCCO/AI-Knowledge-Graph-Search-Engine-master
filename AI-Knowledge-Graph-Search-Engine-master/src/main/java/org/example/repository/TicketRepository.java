package org.example.repository;

import org.example.model.Ticket;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.neo4j.driver.Record;

public class TicketRepository {
    private final Neo4jConnection connection;

    public TicketRepository() {
        this.connection = Neo4jConnection.getInstance();
    }

    /**
     * Create a new ticket with all relationships
     */
    public Ticket create(Ticket ticket) {
        if (ticket.getId() == null || ticket.getId().isEmpty()) {
            ticket.setId(generateTicketId());
        }

        String query = """
                CREATE (t:Ticket {
                    id: $id,
                    title: $title,
                    description: $description,
                    status: $status,
                    priority: $priority,
                    category: $category,
                    assignedTo: $assignedTo,
                    createdBy: $createdBy,
                    createdAt: datetime($createdAt),
                    updatedAt: datetime($updatedAt)
                })
                RETURN t
                """;

        try (Session session = connection.getSession()) {
            // Create the ticket node
            session.run(query, Values.parameters(
                    "id", ticket.getId(),
                    "title", ticket.getTitle(),
                    "description", ticket.getDescription(),
                    "status", ticket.getStatus(),
                    "priority", ticket.getPriority(),
                    "category", ticket.getCategory(),
                    "assignedTo", ticket.getAssignedTo(),
                    "createdBy", ticket.getCreatedBy(),
                    "createdAt", ticket.getCreatedAt().toString(),
                    "updatedAt", ticket.getUpdatedAt().toString()));

            // Create relationships
            createTicketRelationships(ticket);

            System.out.println("✅ Ticket created with relationships: " + ticket.getId());
            return ticket;
        } catch (Exception e) {
            System.err.println("❌ Error creating ticket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create all relationships for a ticket
     */
    private void createTicketRelationships(Ticket ticket) {
        try (Session session = connection.getSession()) {

            // 1. Link to Category
            if (ticket.getCategory() != null && !ticket.getCategory().isEmpty()) {
                String categoryQuery = """
                        MATCH (t:Ticket {id: $ticketId})
                        MATCH (c:Category)
                        WHERE c.name = $categoryName OR c.id = $categoryName
                        MERGE (t)-[r:BELONGS_TO]->(c)
                        SET r.linkedAt = datetime()
                        RETURN r
                        """;
                session.run(categoryQuery, Values.parameters(
                        "ticketId", ticket.getId(),
                        "categoryName", ticket.getCategory()));
                System.out.println("  ✓ Linked to category: " + ticket.getCategory());
            }

            // 2. Link to Creator (User who created the ticket)
            if (ticket.getCreatedBy() != null && !ticket.getCreatedBy().isEmpty()) {
                String creatorQuery = """
                        MATCH (t:Ticket {id: $ticketId})
                        MATCH (u:User)
                        WHERE u.username = $createdBy OR u.id = $createdBy
                        MERGE (u)-[r:CREATED]->(t)
                        SET r.createdAt = datetime($createdAt)
                        RETURN r
                        """;
                session.run(creatorQuery, Values.parameters(
                        "ticketId", ticket.getId(),
                        "createdBy", ticket.getCreatedBy(),
                        "createdAt", ticket.getCreatedAt().toString()));
                System.out.println("  ✓ Linked to creator: " + ticket.getCreatedBy());
            }

            // 3. Link to Assigned User
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().isEmpty()) {
                String assignQuery = """
                        MATCH (t:Ticket {id: $ticketId})
                        MATCH (u:User)
                        WHERE u.username = $assignedTo OR u.id = $assignedTo
                        MERGE (t)-[r:ASSIGNED_TO]->(u)
                        SET r.assignedAt = datetime()
                        RETURN r
                        """;
                session.run(assignQuery, Values.parameters(
                        "ticketId", ticket.getId(),
                        "assignedTo", ticket.getAssignedTo()));
                System.out.println("  ✓ Linked to assignee: " + ticket.getAssignedTo());
            }

            // 4. Link to SLA Policy (by priority)
            if (ticket.getPriority() != null) {
                String slaQuery = """
                        MATCH (t:Ticket {id: $ticketId})
                        MATCH (s:SLA)
                        WHERE s.priority = $priority AND s.active = true
                        MERGE (t)-[r:HAS_SLA]->(s)
                        SET r.appliedAt = datetime()
                        RETURN r
                        """;
                session.run(slaQuery, Values.parameters(
                        "ticketId", ticket.getId(),
                        "priority", ticket.getPriority()));
                System.out.println("  ✓ Linked to SLA policy for priority: " + ticket.getPriority());
            }

            // 5. Create SIMILAR_TO relationships with tickets in same category
            if (ticket.getCategory() != null) {
                String similarQuery = """
                        MATCH (t1:Ticket {id: $ticketId})-[:BELONGS_TO]->(c:Category)
                        MATCH (t2:Ticket)-[:BELONGS_TO]->(c)
                        WHERE t1.id <> t2.id AND NOT (t1)-[:SIMILAR_TO]-(t2)
                        WITH t1, t2, c LIMIT 5
                        MERGE (t1)-[r:SIMILAR_TO]-(t2)
                        SET r.reason = 'Same category: ' + c.name
                        RETURN count(r) as similarCount
                        """;
                Result result = session.run(similarQuery, Values.parameters("ticketId", ticket.getId()));
                if (result.hasNext()) {
                    long count = result.next().get("similarCount").asLong();
                    if (count > 0) {
                        System.out.println("  ✓ Linked to " + count + " similar tickets");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("  ❌ Error creating relationships: " + e.getMessage());
        }
    }

    /**
     * Update ticket and refresh relationships
     */
    public Ticket update(Ticket ticket) {
        ticket.setUpdatedAt(LocalDateTime.now());

        String query = """
                MATCH (t:Ticket {id: $id})
                SET t.title = $title,
                    t.description = $description,
                    t.status = $status,
                    t.priority = $priority,
                    t.category = $category,
                    t.assignedTo = $assignedTo,
                    t.updatedAt = datetime($updatedAt)
                RETURN t
                """;

        try (Session session = connection.getSession()) {
            session.run(query, Values.parameters(
                    "id", ticket.getId(),
                    "title", ticket.getTitle(),
                    "description", ticket.getDescription(),
                    "status", ticket.getStatus(),
                    "priority", ticket.getPriority(),
                    "category", ticket.getCategory(),
                    "assignedTo", ticket.getAssignedTo(),
                    "updatedAt", ticket.getUpdatedAt().toString()));

            // Delete old relationships and recreate
            deleteTicketRelationships(ticket.getId());
            createTicketRelationships(ticket);

            System.out.println("✅ Ticket updated: " + ticket.getId());
            return ticket;
        } catch (Exception e) {
            System.err.println("❌ Error updating ticket: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete all relationships for a ticket
     */
    private void deleteTicketRelationships(String ticketId) {
        try (Session session = connection.getSession()) {
            String query = """
                    MATCH (t:Ticket {id: $ticketId})
                    OPTIONAL MATCH (t)-[r]-()
                    WHERE type(r) IN ['BELONGS_TO', 'ASSIGNED_TO', 'HAS_SLA', 'SIMILAR_TO']
                    DELETE r
                    """;
            session.run(query, Values.parameters("ticketId", ticketId));
        } catch (Exception e) {
            System.err.println("Error deleting relationships: " + e.getMessage());
        }
    }

    /**
     * Get all tickets
     */
    public List<Ticket> findAll() {
        String query = "MATCH (t:Ticket) RETURN t AS t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                try {
                    tickets.add(mapToTicket(result.next()));
                } catch (Exception e) {
                    System.err.println("⚠️ Skipping invalid ticket record: " + e.getMessage());
                }
            }
            System.out.println("✅ Found " + tickets.size() + " tickets");
        } catch (Exception e) {
            System.err.println("❌ Error fetching tickets: " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    /**
     * Find ticket by ID with relationships
     */
    public Ticket findById(String id) {
        String query = """
                MATCH (t:Ticket {id: $id})
                OPTIONAL MATCH (t)-[:BELONGS_TO]->(c:Category)
                OPTIONAL MATCH (t)-[:ASSIGNED_TO]->(u:User)
                OPTIONAL MATCH (creator:User)-[:CREATED]->(t)
                RETURN t, c.name as categoryName, u.username as assignedUser, creator.username as creatorName
                """;

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("id", id));
            if (result.hasNext()) {
                return mapToTicket(result.next());
            }
        } catch (Exception e) {
            System.err.println("❌ Error finding ticket: " + e.getMessage());
        }

        return null;
    }

    /**
     * Delete ticket and all its relationships
     */
    public boolean delete(String id) {
        String query = "MATCH (t:Ticket {id: $id}) DETACH DELETE t";

        try (Session session = connection.getSession()) {
            session.run(query, Values.parameters("id", id));
            System.out.println("✅ Ticket deleted: " + id);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error deleting ticket: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save ticket (create or update)
     */
    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null || ticket.getId().isEmpty() || findById(ticket.getId()) == null) {
            return create(ticket);
        } else {
            return update(ticket);
        }
    }

    /**
     * Find tickets by status
     */
    public List<Ticket> findByStatus(org.example.model.enums.TicketStatus status) {
        String query = "MATCH (t:Ticket {status: $status}) RETURN t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("status", status.name()));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
        } catch (Exception e) {
            System.err.println("❌ Error finding tickets by status: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * Find tickets by priority
     */
    public List<Ticket> findByPriority(org.example.model.enums.Priority priority) {
        String query = "MATCH (t:Ticket {priority: $priority}) RETURN t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("priority", priority.name()));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
        } catch (Exception e) {
            System.err.println("❌ Error finding tickets by priority: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * Find tickets by assignee
     */
    public List<Ticket> findByAssignee(String assigneeId) {
        String query = """
                MATCH (t:Ticket)-[:ASSIGNED_TO]->(u:User)
                WHERE u.id = $assigneeId OR u.username = $assigneeId
                RETURN t ORDER BY t.createdAt DESC
                """;
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("assigneeId", assigneeId));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
        } catch (Exception e) {
            System.err.println("❌ Error finding tickets by assignee: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * Get total ticket count
     */
    public long count() {
        String query = "MATCH (t:Ticket) RETURN count(t) as count";

        try (Session session = connection.getSession()) {
            Result result = session.run(query);
            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }
        } catch (Exception e) {
            System.err.println("❌ Error counting tickets: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get ticket count by status (enum version)
     */
    public long countByStatus(org.example.model.enums.TicketStatus status) {
        String query = "MATCH (t:Ticket {status: $status}) RETURN count(t) as count";

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("status", status.name()));
            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }
        } catch (Exception e) {
            System.err.println("❌ Error counting tickets by status: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get ticket count by status (String version)
     */
    public long countByStatus(String status) {
        String query = "MATCH (t:Ticket {status: $status}) RETURN count(t) as count";

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("status", status));
            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }
        } catch (Exception e) {
            System.err.println("❌ Error counting tickets: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get ticket count by priority
     */
    public long countByPriority(org.example.model.enums.Priority priority) {
        String query = "MATCH (t:Ticket {priority: $priority}) RETURN count(t) as count";

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("priority", priority.name()));
            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }
        } catch (Exception e) {
            System.err.println("❌ Error counting tickets by priority: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Search tickets by title or description
     */
    public List<Ticket> search(String keyword) {
        String query = """
                MATCH (t:Ticket)
                WHERE toLower(t.title) CONTAINS toLower($keyword)
                   OR toLower(t.description) CONTAINS toLower($keyword)
                RETURN t
                ORDER BY t.createdAt DESC
                """;

        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("keyword", keyword));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
        } catch (Exception e) {
            System.err.println("❌ Error searching tickets: " + e.getMessage());
        }

        return tickets;
    }

    /**
     * Map Neo4j record to Ticket object
     */
    private Ticket mapToTicket(Record record) {
        var node = record.get("t").asNode();

        Ticket ticket = new Ticket();
        // Safely map fields with defaults if necessary
        ticket.setId(node.get("id").asString("UNKNOWN_ID"));
        ticket.setTitle(node.get("title").asString("Untitled Ticket"));
        ticket.setDescription(node.get("description").asString(""));
        ticket.setStatus(node.get("status").asString("OPEN"));
        ticket.setPriority(node.get("priority").asString("MEDIUM"));
        ticket.setCategory(node.get("category").asString("General"));
        ticket.setAssignedTo(node.get("assignedTo").asString(""));
        ticket.setCreatedBy(node.get("createdBy").asString(""));

        // Robust date handling
        if (!node.get("createdAt").isNull()) {
            try {
                // 1. Try native ZonedDateTime (best for Neo4j datetime())
                ticket.setCreatedAt(node.get("createdAt").asZonedDateTime().toLocalDateTime());
            } catch (Exception e1) {
                try {
                    // 2. Try LocalDateTime (if stored as localdatetime())
                    ticket.setCreatedAt(node.get("createdAt").asLocalDateTime());
                } catch (Exception e2) {
                    try {
                        // 3. Try parsing string representation
                        String dateStr = node.get("createdAt").asString();
                        // Remove quotes if present
                        dateStr = dateStr.replace("\"", "");
                        // Handle Z suffix manually if LocalDateTime.parse fails
                        if (dateStr.endsWith("Z")) {
                            ticket.setCreatedAt(java.time.ZonedDateTime.parse(dateStr).toLocalDateTime());
                        } else {
                            ticket.setCreatedAt(LocalDateTime.parse(dateStr));
                        }
                    } catch (Exception e3) {
                        System.err.println("⚠️ Could not parse createdAt for ticket " + ticket.getId() + ": "
                                + node.get("createdAt"));
                        ticket.setCreatedAt(LocalDateTime.now());
                    }
                }
            }
        } else {
            ticket.setCreatedAt(LocalDateTime.now());
        }

        if (!node.get("updatedAt").isNull()) {
            try {
                ticket.setUpdatedAt(node.get("updatedAt").asLocalDateTime());
            } catch (Exception e) {
                ticket.setUpdatedAt(LocalDateTime.now());
            }
        }

        return ticket;
    }

    /**
     * Search tickets with filters
     */
    public List<Ticket> searchTickets(String keyword, String status, String priority, String assigneeId) {
        StringBuilder query = new StringBuilder("MATCH (t:Ticket) WHERE 1=1 ");
        Map<String, Object> params = new HashMap<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            query.append(
                    "AND (toLower(t.title) CONTAINS toLower($keyword) OR toLower(t.description) CONTAINS toLower($keyword)) ");
            params.put("keyword", keyword.trim());
        }

        if (status != null && !status.isEmpty() && !"All".equalsIgnoreCase(status)
                && !"Status: All".equalsIgnoreCase(status)) {
            query.append("AND toLower(t.status) = toLower($status) ");
            params.put("status", status);
        }

        if (priority != null && !priority.isEmpty() && !"All".equalsIgnoreCase(priority)
                && !"Severity: All".equalsIgnoreCase(priority)) {
            query.append("AND toLower(t.priority) = toLower($priority) ");
            params.put("priority", priority);
        }

        if (assigneeId != null && !assigneeId.isEmpty()) {
            // Check both property and relationship for robustness, case-insensitive
            query.append(
                    "AND (toLower(t.assignedTo) = toLower($assigneeId) OR EXISTS { MATCH (t)-[:ASSIGNED_TO]->(u:User) WHERE toLower(u.username) = toLower($assigneeId) }) ");
            params.put("assigneeId", assigneeId);
        }

        query.append("RETURN t ORDER BY t.createdAt DESC");

        List<Ticket> tickets = new ArrayList<>();
        try (Session session = connection.getSession()) {
            System.out.println("🔍 Executing Query: " + query.toString());
            System.out.println("🔍 Params: " + params);

            Result result = session.run(query.toString(), params);
            while (result.hasNext()) {
                Ticket t = mapToTicket(result.next());
                tickets.add(t);
                // System.out.println(" > Found ticket: " + t.getId() + " assigned to: " +
                // t.getAssignedTo());
            }
            System.out.println("✅ Search found " + tickets.size() + " tickets");

            if (tickets.isEmpty() && assigneeId != null) {
                // Debug: Check why we didn't find anything for this user
                System.out.println("⚠️ Debug: Checking available assignees in DB...");
                Result check = session.run("MATCH (t:Ticket) RETURN DISTINCT t.assignedTo as assignee LIMIT 10");
                while (check.hasNext()) {
                    System.out.println("   - DB has assignee: '" + check.next().get("assignee").asString() + "'");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error searching tickets: " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    /**
     * Get ticket count by category
     */
    public Map<String, Long> getTicketsByCategory() {
        String query = "MATCH (t:Ticket) RETURN t.category as category, count(t) as count ORDER BY count DESC";
        Map<String, Long> stats = new HashMap<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                String category = record.get("category").asString("Unknown");
                long count = record.get("count").asLong();
                stats.put(category, count);
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching category stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Get ticket count by status
     */
    public Map<String, Long> getTicketsByStatus() {
        String query = "MATCH (t:Ticket) RETURN t.status as status, count(t) as count";
        Map<String, Long> stats = new HashMap<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                String status = record.get("status").asString("Unknown");
                long count = record.get("count").asLong();
                stats.put(status, count);
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching status stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Get tickets created over the last 30 days
     */
    public Map<String, Long> getTicketsTrend() {
        String query = """
                MATCH (t:Ticket)
                WHERE t.createdAt >= datetime() - duration({days: 30})
                RETURN substring(toString(t.createdAt), 0, 10) as date, count(t) as count
                ORDER BY date
                """;
        Map<String, Long> stats = new HashMap<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                stats.put(record.get("date").asString(), record.get("count").asLong());
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching trend stats: " + e.getMessage());
        }
        return stats;
    }

    /**
     * Generate unique ticket ID
     */
    private String generateTicketId() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}