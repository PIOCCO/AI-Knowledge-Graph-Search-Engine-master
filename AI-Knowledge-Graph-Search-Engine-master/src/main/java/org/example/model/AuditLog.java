package org.example.model;

import java.time.LocalDateTime;

public class AuditLog {
    private String id;
    private String action;
    private String userId;
    private String username;
    private String entityType;
    private String entityId;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private String result;

    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String id, String action, String userId, String entityType, String entityId) {
        this();
        this.id = id;
        this.action = action;
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id='" + id + '\'' +
                ", action='" + action + '\'' +
                ", user='" + username + '\'' +
                ", entity=" + entityType + ":" + entityId +
                ", timestamp=" + timestamp +
                '}';
    }
}
