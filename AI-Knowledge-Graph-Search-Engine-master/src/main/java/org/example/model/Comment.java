package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Comment {
    private String id;
    private String content;
    private String ticketId;
    private String authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isInternal;
    private List<String> attachmentIds;
    private boolean edited;

    public Comment() {
        this.attachmentIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.isInternal = false;
        this.edited = false;
    }

    public Comment(String id, String content, String ticketId, String authorId) {
        this();
        this.id = id;
        this.content = content;
        this.ticketId = ticketId;
        this.authorId = authorId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
        this.edited = true;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
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

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    public List<String> getAttachmentIds() {
        return attachmentIds;
    }

    public void setAttachmentIds(List<String> attachmentIds) {
        this.attachmentIds = attachmentIds;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", authorName='" + authorName + '\'' +
                ", createdAt=" + createdAt +
                ", edited=" + edited +
                '}';
    }
}
