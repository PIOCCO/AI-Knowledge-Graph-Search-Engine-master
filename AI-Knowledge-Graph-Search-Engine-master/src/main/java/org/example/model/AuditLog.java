package org.example.model;

import java.time.LocalDateTime;

/**
 * Audit Log Model - System activity tracking
 */
public class AuditLog {

    private String id;
    private String action;
    private String userId;
    private String username;
    private String entityType;
    private String entityId;
    private String oldValue;
    private String newValue;
    private String result;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;

    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
        this.result = "SUCCESS";
    }

    public AuditLog(String action, String userId, String entityType, String entityId) {
        this();
        this.action = action;
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s by %s on %s:%s - %s",
                timestamp, action, username, entityType, entityId, result);
    }
}