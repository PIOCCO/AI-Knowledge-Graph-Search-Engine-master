package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private String id;
    private String name;
    private String description;
    private String leadId;
    private List<String> memberIds;
    private String department;
    private boolean active;
    private LocalDateTime createdAt;
    private int maxCapacity;

    public Team() {
        this.memberIds = new ArrayList<>();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.maxCapacity = 10;
    }

    public Team(String id, String name, String leadId) {
        this();
        this.id = id;
        this.name = name;
        this.leadId = leadId;
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

    public String getLeadId() {
        return leadId;
    }

    public void setLeadId(String leadId) {
        this.leadId = leadId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public void addMember(String userId) {
        if (!memberIds.contains(userId) && memberIds.size() < maxCapacity) {
            memberIds.add(userId);
        }
    }

    public void removeMember(String userId) {
        memberIds.remove(userId);
    }

    @Override
    public String toString() {
        return "Team{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", leadId='" + leadId + '\'' +
                ", memberCount=" + memberIds.size() +
                '}';
    }
}
