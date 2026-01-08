package org.example.model;

import java.time.LocalDateTime;

/**
 * Comment Model - Ticket comments and discussions
 */
public class Comment {

    private String id;
    private String ticketId;
    private String content;
    private String authorId;
    private String authorName;
    private boolean isInternal; // Internal comments visible only to agents
    private boolean edited;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Comment() {
        this.createdAt = LocalDateTime.now();
        this.isInternal = false;
        this.edited = false;
    }

    public Comment(String ticketId, String content, String authorId) {
        this();
        this.ticketId = ticketId;
        this.content = content;
        this.authorId = authorId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.edited = true;
        this.updatedAt = LocalDateTime.now();
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        this.isInternal = internal;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return String.format("Comment[%s] by %s on %s",
                id, authorName, createdAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Comment other = (Comment) obj;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}