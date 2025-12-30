package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase {
    private String id;
    private String title;
    private String content;
    private String categoryId;
    private String authorId;
    private List<String> tags;
    private int viewCount;
    private int helpfulCount;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String relatedTicketIds;

    public KnowledgeBase() {
        this.tags = new ArrayList<>();
        this.viewCount = 0;
        this.helpfulCount = 0;
        this.published = false;
        this.createdAt = LocalDateTime.now();
    }

    public KnowledgeBase(String id, String title, String content) {
        this();
        this.id = id;
        this.title = title;
        this.content = content;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
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

    public String getRelatedTicketIds() {
        return relatedTicketIds;
    }

    public void setRelatedTicketIds(String relatedTicketIds) {
        this.relatedTicketIds = relatedTicketIds;
    }

    @Override
    public String toString() {
        return "KnowledgeBase{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", views=" + viewCount +
                ", helpful=" + helpfulCount +
                '}';
    }
}
