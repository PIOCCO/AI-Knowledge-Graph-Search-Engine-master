package org.example.model;

import org.example.model.enums.Priority;
import java.time.LocalDateTime;

public class SLA {
    private String id;
    private String name;
    private String description;
    private Priority priority;
    private int responseTimeMinutes;
    private int resolutionTimeMinutes;
    private boolean active;
    private LocalDateTime createdAt;
    private String categoryId;

    public SLA() {
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public SLA(String id, String name, Priority priority, int responseTimeMinutes, int resolutionTimeMinutes) {
        this();
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.responseTimeMinutes = responseTimeMinutes;
        this.resolutionTimeMinutes = resolutionTimeMinutes;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getResponseTimeMinutes() {
        return responseTimeMinutes;
    }

    public void setResponseTimeMinutes(int responseTimeMinutes) {
        this.responseTimeMinutes = responseTimeMinutes;
    }

    public int getResolutionTimeMinutes() {
        return resolutionTimeMinutes;
    }

    public void setResolutionTimeMinutes(int resolutionTimeMinutes) {
        this.resolutionTimeMinutes = resolutionTimeMinutes;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getResponseTimeFormatted() {
        return formatMinutes(responseTimeMinutes);
    }

    public String getResolutionTimeFormatted() {
        return formatMinutes(resolutionTimeMinutes);
    }

    private String formatMinutes(int minutes) {
        if (minutes < 60)
            return minutes + " minutes";
        if (minutes < 1440)
            return (minutes / 60) + " hours";
        return (minutes / 1440) + " days";
    }

    @Override
    public String toString() {
        return "SLA{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", responseTime=" + getResponseTimeFormatted() +
                ", resolutionTime=" + getResolutionTimeFormatted() +
                '}';
    }
}
