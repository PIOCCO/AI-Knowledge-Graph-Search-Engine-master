package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ClassificationResult {

    @JsonProperty("ticketId")
    private String ticketId;

    @JsonProperty("predictedCategory")
    private String predictedCategory;

    @JsonProperty("categoryName")
    private String categoryName;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("alternatives")
    private List<Alternative> alternatives;

    @JsonProperty("timestamp")
    private String timestamp;

    // Constructor
    public ClassificationResult() {}

    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getPredictedCategory() { return predictedCategory; }
    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<Alternative> getAlternatives() { return alternatives; }
    public void setAlternatives(List<Alternative> alternatives) {
        this.alternatives = alternatives;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("ClassificationResult{category=%s, confidence=%.2f%%}",
                categoryName, confidence * 100);
    }

    // Inner class for alternatives
    public static class Alternative {
        @JsonProperty("categoryId")
        private String categoryId;

        @JsonProperty("categoryName")
        private String categoryName;

        @JsonProperty("confidence")
        private double confidence;

        public Alternative() {}

        public String getCategoryId() { return categoryId; }
        public void setCategoryId(String categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }
    }
}