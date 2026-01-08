package org.example.model;

import java.time.LocalDateTime;

/**
 * Notification Model - User notifications
 */
public class Notification {

    private String id;
    private String userId;
    private String message;
    private String type;
    private String relatedEntityId;
    private String relatedEntityType;
    private boolean read;
    private String priority;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // Constructors
    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
        this.priority = "NORMAL";
    }

    public Notification(String userId, String message, String type) {
        this();
        this.userId = userId;
        this.message = message;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(String relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public boolean isRead() { return read; }
    public void setRead(boolean read) {
        this.read = read;
        if (read && readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s",
                type, read ? "READ" : "UNREAD", message);
    }
}