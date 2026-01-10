package org.example.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Report Model - Generated reports data
 */
public class Report {

    private String id;
    private String name;
    private String type;
    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String generatedBy;
    private Map<String, Object> data;
    private String previewText;
    private boolean includeCharts;
    private boolean includeSummary;
    private boolean includeDetails;

    // Constructors
    public Report() {
        this.generatedAt = LocalDateTime.now();
    }

    public Report(String name, String type) {
        this();
        this.name = name;
        this.type = type;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) {
        this.generatedBy = generatedBy;
    }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public String getPreviewText() { return previewText; }
    public void setPreviewText(String previewText) {
        this.previewText = previewText;
    }

    public boolean isIncludeCharts() { return includeCharts; }
    public void setIncludeCharts(boolean includeCharts) {
        this.includeCharts = includeCharts;
    }

    public boolean isIncludeSummary() { return includeSummary; }
    public void setIncludeSummary(boolean includeSummary) {
        this.includeSummary = includeSummary;
    }

    public boolean isIncludeDetails() { return includeDetails; }
    public void setIncludeDetails(boolean includeDetails) {
        this.includeDetails = includeDetails;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Generated: %s",
                name, type, generatedAt);
    }
}