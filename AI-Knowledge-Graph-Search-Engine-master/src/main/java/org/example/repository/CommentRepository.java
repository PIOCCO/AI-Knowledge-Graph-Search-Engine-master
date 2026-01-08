package org.example.repository;

import org.example.model.Comment;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.util.*;
import org.neo4j.driver.Record;


import static org.neo4j.driver.Values.parameters;

/**
 * Comment Repository - Ticket comments data access
 */
public class CommentRepository {

    private final Driver driver;

    public CommentRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    /**
     * Save or update comment
     */
    public void save(Comment comment) {
        try (Session session = driver.session()) {
            String query = "MERGE (c:Comment {id: $id}) " +
                    "SET c.ticketId = $ticketId, " +
                    "c.content = $content, " +
                    "c.authorId = $authorId, " +
                    "c.authorName = $authorName, " +
                    "c.isInternal = $isInternal, " +
                    "c.edited = $edited, " +
                    "c.createdAt = $createdAt, " +
                    "c.updatedAt = $updatedAt";

            session.run(query, parameters(
                    "id", comment.getId(),
                    "ticketId", comment.getTicketId(),
                    "content", comment.getContent(),
                    "authorId", comment.getAuthorId(),
                    "authorName", comment.getAuthorName(),
                    "isInternal", comment.isInternal(),
                    "edited", comment.isEdited(),
                    "createdAt", comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null,
                    "updatedAt", comment.getUpdatedAt() != null ? comment.getUpdatedAt().toString() : null
            ));

            // Create relationship to ticket
            String relationQuery = "MATCH (t:Ticket {id: $ticketId}), (c:Comment {id: $commentId}) " +
                    "MERGE (t)-[:HAS_COMMENT]->(c)";

            session.run(relationQuery, parameters(
                    "ticketId", comment.getTicketId(),
                    "commentId", comment.getId()
            ));

            System.out.println("✅ Comment saved: " + comment.getId());
        } catch (Exception e) {
            System.err.println("❌ Error saving comment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find comments by ticket ID
     */
    public List<Comment> findByTicketId(String ticketId) {
        try (Session session = driver.session()) {
            String query = "MATCH (t:Ticket {id: $ticketId})-[:HAS_COMMENT]->(c:Comment) " +
                    "RETURN c ORDER BY c.createdAt ASC";

            Result result = session.run(query, parameters("ticketId", ticketId));
            List<Comment> comments = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                comments.add(mapToComment(record.get("c").asMap()));
            }

            return comments;
        } catch (Exception e) {
            System.err.println("❌ Error finding comments: " + e.getMessage());
            return createSampleComments(ticketId); // Return sample data if DB fails
        }
    }

    /**
     * Find comment by ID
     */
    public Comment findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Comment {id: $id}) RETURN c";

            Result result = session.run(query, parameters("id", id));

            if (result.hasNext()) {
                return mapToComment(result.next().get("c").asMap());
            }

            return null;
        } catch (Exception e) {
            System.err.println("❌ Error finding comment: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete comment
     */
    public void delete(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Comment {id: $id}) DETACH DELETE c";
            session.run(query, parameters("id", id));
            System.out.println("✅ Comment deleted: " + id);
        } catch (Exception e) {
            System.err.println("❌ Error deleting comment: " + e.getMessage());
        }
    }

    /**
     * Count comments for ticket
     */
    public long countByTicketId(String ticketId) {
        try (Session session = driver.session()) {
            String query = "MATCH (t:Ticket {id: $ticketId})-[:HAS_COMMENT]->(c:Comment) " +
                    "RETURN count(c) as count";

            Result result = session.run(query, parameters("ticketId", ticketId));

            if (result.hasNext()) {
                return result.next().get("count").asLong();
            }

            return 0;
        } catch (Exception e) {
            System.err.println("❌ Error counting comments: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Find recent comments by author
     */
    public List<Comment> findRecentByAuthor(String authorId, int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Comment {authorId: $authorId}) " +
                    "RETURN c ORDER BY c.createdAt DESC LIMIT $limit";

            Result result = session.run(query, parameters("authorId", authorId, "limit", limit));
            List<Comment> comments = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                comments.add(mapToComment(record.get("c").asMap()));
            }

            return comments;
        } catch (Exception e) {
            System.err.println("❌ Error finding recent comments: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Private helper methods

    private Comment mapToComment(Map<String, Object> map) {
        Comment comment = new Comment();

        comment.setId((String) map.get("id"));
        comment.setTicketId((String) map.get("ticketId"));
        comment.setContent((String) map.get("content"));
        comment.setAuthorId((String) map.get("authorId"));
        comment.setAuthorName((String) map.get("authorName"));

        if (map.get("isInternal") != null) {
            comment.setInternal((Boolean) map.get("isInternal"));
        }

        if (map.get("edited") != null) {
            comment.setEdited((Boolean) map.get("edited"));
        }

        if (map.get("createdAt") != null) {
            comment.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }

        if (map.get("updatedAt") != null) {
            comment.setUpdatedAt(LocalDateTime.parse((String) map.get("updatedAt")));
        }

        return comment;
    }

    /**
     * Create sample comments for demo
     */
    private List<Comment> createSampleComments(String ticketId) {
        List<Comment> comments = new ArrayList<>();

        Comment c1 = new Comment(ticketId, "I'm experiencing the same issue. Has anyone found a solution?", "user-001");
        c1.setId("CMT-001");
        c1.setAuthorName("John Doe");
        c1.setCreatedAt(LocalDateTime.now().minusHours(2));
        comments.add(c1);

        Comment c2 = new Comment(ticketId, "We're investigating this issue. Will update shortly.", "admin-001");
        c2.setId("CMT-002");
        c2.setAuthorName("Support Agent");
        c2.setCreatedAt(LocalDateTime.now().minusHours(1));
        comments.add(c2);

        Comment c3 = new Comment(ticketId, "This has been resolved. Please try clearing your cache and reloading.", "admin-001");
        c3.setId("CMT-003");
        c3.setAuthorName("Support Agent");
        c3.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        comments.add(c3);

        return comments;
    }
}