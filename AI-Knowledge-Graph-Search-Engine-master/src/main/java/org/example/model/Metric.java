package org.example.model;

import java.time.LocalDateTime;

public class Metric {
    private String id;
    private String name;
    private String description;
    private String metricType;
    private double value;
    private String unit;
    private LocalDateTime timestamp;
    private String entityId;
    private String entityType;
    private String period;

    public Metric() {
        this.timestamp = LocalDateTime.now();
    }

    public Metric(String id, String name, String metricType, double value) {
        this();
        this.id = id;
        this.name = name;
        this.metricType = metricType;
        this.value = value;
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

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getFormattedValue() {
        if (unit != null) {
            return String.format("%.2f %s", value, unit);
        }
        return String.format("%.2f", value);
    }

    @Override
    public String toString() {
        return "Metric{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", value=" + getFormattedValue() +
                ", timestamp=" + timestamp +
                '}';
    }
}
