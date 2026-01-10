package org.example.repository;

import org.example.model.Ticket;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TicketRepository {
    private final Neo4jConnection connection;
    // Create relationship to category
    String relationQuery = "MATCH (t:Ticket {id: $ticketId}), (c:Category {id: $categoryId}) " +
            "MERGE (t)-[:BELONGS_TO]->(c)";
    public TicketRepository() {
        this.connection = Neo4jConnection.getInstance();
    }

    // Create a new ticket
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
            session.run(query,
                    Values.parameters(
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
            System.out.println("✅ Ticket created: " + ticket.getId());
            return ticket;
        } catch (Exception e) {
            System.err.println("❌ Error creating ticket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Get all tickets
    public List<Ticket> findAll() {
        String query = "MATCH (t:Ticket) RETURN t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query);
            while (result.hasNext()) {
                Record record = result.next();
                tickets.add(mapToTicket(record));
            }
            System.out.println("✅ Found " + tickets.size() + " tickets");
        } catch (Exception e) {
            System.err.println("❌ Error fetching tickets: " + e.getMessage());
            e.printStackTrace();
        }

        return tickets;
    }

    // Find ticket by ID
    public Ticket findById(String id) {
        String query = "MATCH (t:Ticket {id: $id}) RETURN t";

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

    // Update ticket
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
            session.run(query,
                    Values.parameters(
                            "id", ticket.getId(),
                            "title", ticket.getTitle(),
                            "description", ticket.getDescription(),
                            "status", ticket.getStatus(),
                            "priority", ticket.getPriority(),
                            "category", ticket.getCategory(),
                            "assignedTo", ticket.getAssignedTo(),
                            "updatedAt", ticket.getUpdatedAt().toString()));
            System.out.println("✅ Ticket updated: " + ticket.getId());
            return ticket;
        } catch (Exception e) {
            System.err.println("❌ Error updating ticket: " + e.getMessage());
            return null;
        }
    }

    // Delete ticket
    public boolean delete(String id) {
        String query = "MATCH (t:Ticket {id: $id}) DELETE t";

        try (Session session = connection.getSession()) {
            session.run(query, Values.parameters("id", id));
            System.out.println("✅ Ticket deleted: " + id);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error deleting ticket: " + e.getMessage());
            return false;
        }
    }

    // Save ticket (create or update)
    public Ticket save(Ticket ticket) {
        if (ticket.getId() == null || ticket.getId().isEmpty() || findById(ticket.getId()) == null) {
            return create(ticket);
        } else {
            return update(ticket);
        }
    }

    // Find tickets by status
    public List<Ticket> findByStatus(org.example.model.enums.TicketStatus status) {
        String query = "MATCH (t:Ticket {status: $status}) RETURN t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("status", status.name()));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
            System.out.println("✅ Found " + tickets.size() + " tickets with status: " + status);
        } catch (Exception e) {
            System.err.println("❌ Error finding tickets by status: " + e.getMessage());
        }

        return tickets;
    }

    // Find tickets by priority
    public List<Ticket> findByPriority(org.example.model.enums.Priority priority) {
        String query = "MATCH (t:Ticket {priority: $priority}) RETURN t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("priority", priority.name()));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
            System.out.println("✅ Found " + tickets.size() + " tickets with priority: " + priority);
        } catch (Exception e) {
            System.err.println("❌ Error finding tickets by priority: " + e.getMessage());
        }

        return tickets;
    }

    // Find tickets by assignee
    public List<Ticket> findByAssignee(String assigneeId) {
        String query = "MATCH (t:Ticket {assignedTo: $assigneeId}) RETURN t ORDER BY t.createdAt DESC";
        List<Ticket> tickets = new ArrayList<>();

        try (Session session = connection.getSession()) {
            Result result = session.run(query, Values.parameters("assigneeId", assigneeId));
            while (result.hasNext()) {
                tickets.add(mapToTicket(result.next()));
            }
            System.out.println("✅ Found " + tickets.size() + " tickets assigned to: " + assigneeId);
        } catch (Exception e) {
            System.err.println("❌ Error finding tickets by assignee: " + e.getMessage());
        }

        return tickets;
    }

    // Get total ticket count
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

    // Get ticket count by status (enum version)
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

    // Get ticket count by status (String version - for backward compatibility)
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

    // Get ticket count by priority
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

    // Search tickets by title or description
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

    private Ticket mapToTicket(Record record) {
        var node = record.get("t").asNode();

        Ticket ticket = new Ticket();
        ticket.setId(node.get("id").asString());
        ticket.setTitle(node.get("title").asString());
        ticket.setDescription(node.get("description").asString(""));
        ticket.setStatus(node.get("status").asString());
        ticket.setPriority(node.get("priority").asString());
        ticket.setCategory(node.get("category").asString());
        ticket.setAssignedTo(node.get("assignedTo").asString(""));
        ticket.setCreatedBy(node.get("createdBy").asString(""));

        if (!node.get("createdAt").isNull()) {
            ticket.setCreatedAt(node.get("createdAt").asLocalDateTime());
        }

        if (!node.get("updatedAt").isNull()) {
            ticket.setUpdatedAt(node.get("updatedAt").asLocalDateTime());
        }

        return ticket;
    }

    // Generate unique ticket ID
    private String generateTicketId() {
        return "TKT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}