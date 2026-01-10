package org.example.model;

import java.time.LocalDateTime;

public class Ticket {
    private String id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String category;
    private String categoryId; // Added for service compatibility
    private String assignedTo;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate; // Added for SLA tracking
    private LocalDateTime resolvedAt; // Added for resolution tracking
    // ML Classification Fields
    private String mlPredictedCategory;
    private Double mlConfidence;
    private LocalDateTime mlClassifiedAt;
    private boolean autoClassified;

    public Ticket() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Ticket(String id, String title, String description, String status,
            String priority, String category, String assignedTo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.category = category;
        this.categoryId = category; // Set both for compatibility
        this.assignedTo = assignedTo;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.categoryId = category; // Keep in sync
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
        this.category = categoryId; // Keep in sync
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getCreatedAtFormatted() {
        if (createdAt == null)
            return "";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getMlPredictedCategory() { return mlPredictedCategory; }
    public void setMlPredictedCategory(String mlPredictedCategory) {
        this.mlPredictedCategory = mlPredictedCategory;
    }

    public Double getMlConfidence() { return mlConfidence; }
    public void setMlConfidence(Double mlConfidence) {
        this.mlConfidence = mlConfidence;
    }

    public LocalDateTime getMlClassifiedAt() { return mlClassifiedAt; }
    public void setMlClassifiedAt(LocalDateTime mlClassifiedAt) {
        this.mlClassifiedAt = mlClassifiedAt;
    }

    public boolean isAutoClassified() { return autoClassified; }
    public void setAutoClassified(boolean autoClassified) {
        this.autoClassified = autoClassified;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}