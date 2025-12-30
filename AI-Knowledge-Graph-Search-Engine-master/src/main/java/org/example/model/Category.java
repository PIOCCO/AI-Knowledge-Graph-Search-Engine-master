package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Category {
    private String id;
    private String name;
    private String description;
    private String parentCategoryId;
    private List<String> subCategoryIds;
    private String color;
    private String icon;
    private boolean active;
    private LocalDateTime createdAt;
    private int ticketCount;

    public Category() {
        this.subCategoryIds = new ArrayList<>();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.ticketCount = 0;
    }

    public Category(String id, String name) {
        this();
        this.id = id;
        this.name = name;
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

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public List<String> getSubCategoryIds() {
        return subCategoryIds;
    }

    public void setSubCategoryIds(List<String> subCategoryIds) {
        this.subCategoryIds = subCategoryIds;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public int getTicketCount() {
        return ticketCount;
    }

    public void setTicketCount(int ticketCount) {
        this.ticketCount = ticketCount;
    }

    public void incrementTicketCount() {
        this.ticketCount++;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ticketCount=" + ticketCount +
                '}';
    }
}
