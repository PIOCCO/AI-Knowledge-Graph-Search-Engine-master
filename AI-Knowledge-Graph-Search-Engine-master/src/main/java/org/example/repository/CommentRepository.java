package org.example.repository;

import org.example.model.Comment;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class CommentRepository {
    private final Driver driver;

    public CommentRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public Comment save(Comment comment) {
        try (Session session = driver.session()) {
            String query = "MERGE (c:Comment {id: $id}) " +
                    "SET c.content = $content, c.ticketId = $ticketId, c.authorId = $authorId, " +
                    "c.authorName = $authorName, c.createdAt = $createdAt, c.isInternal = $isInternal, " +
                    "c.edited = $edited RETURN c";

            session.run(query, parameters(
                    "id", comment.getId(),
                    "content", comment.getContent(),
                    "ticketId", comment.getTicketId(),
                    "authorId", comment.getAuthorId(),
                    "authorName", comment.getAuthorName(),
                    "createdAt",
                    comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : LocalDateTime.now().toString(),
                    "isInternal", comment.isInternal(),
                    "edited", comment.isEdited()));
            return comment;
        }
    }

    public List<Comment> findByTicketId(String ticketId) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Comment {ticketId: $ticketId}) RETURN c ORDER BY c.createdAt";
            Result result = session.run(query, parameters("ticketId", ticketId));
            List<Comment> comments = new ArrayList<>();
            while (result.hasNext()) {
                comments.add(mapToComment(result.next().get("c").asMap()));
            }
            return comments;
        }
    }

    public void delete(String id) {
        try (Session session = driver.session()) {
            session.run("MATCH (c:Comment {id: $id}) DETACH DELETE c", parameters("id", id));
        }
    }

    private Comment mapToComment(Map<String, Object> map) {
        Comment comment = new Comment();
        comment.setId((String) map.get("id"));
        comment.setContent((String) map.get("content"));
        comment.setTicketId((String) map.get("ticketId"));
        comment.setAuthorId((String) map.get("authorId"));
        comment.setAuthorName((String) map.get("authorName"));
        comment.setInternal(map.get("isInternal") != null ? (Boolean) map.get("isInternal") : false);
        comment.setEdited(map.get("edited") != null ? (Boolean) map.get("edited") : false);
        if (map.get("createdAt") != null) {
            comment.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return comment;
    }
}
