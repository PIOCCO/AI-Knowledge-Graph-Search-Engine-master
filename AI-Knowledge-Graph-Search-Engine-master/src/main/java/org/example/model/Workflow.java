package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Workflow {
    private String id;
    private String name;
    private String description;
    private List<WorkflowStep> steps;
    private String triggerEvent;
    private boolean active;
    private LocalDateTime createdAt;
    private String createdBy;
    private int executionCount;

    public Workflow() {
        this.steps = new ArrayList<>();
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.executionCount = 0;
    }

    public Workflow(String id, String name, String triggerEvent) {
        this();
        this.id = id;
        this.name = name;
        this.triggerEvent = triggerEvent;
    }

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

    public List<WorkflowStep> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }

    public String getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(String triggerEvent) {
        this.triggerEvent = triggerEvent;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public void incrementExecutionCount() {
        this.executionCount++;
    }

    public void addStep(WorkflowStep step) {
        this.steps.add(step);
    }

    public static class WorkflowStep {
        private int order;
        private String action;
        private String condition;
        private String parameters;

        public WorkflowStep(int order, String action) {
            this.order = order;
            this.action = action;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public String getParameters() {
            return parameters;
        }

        public void setParameters(String parameters) {
            this.parameters = parameters;
        }
    }

    @Override
    public String toString() {
        return "Workflow{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", steps=" + steps.size() +
                ", executions=" + executionCount +
                '}';
    }
}
