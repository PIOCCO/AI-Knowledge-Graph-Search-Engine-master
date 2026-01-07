package org.example.service;

import org.example.model.Ticket;
import org.example.model.KnowledgeBase;
import org.example.repository.TicketRepository;
import org.example.repository.KBRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Search Service with AI-powered semantic search
 */
public class AdvancedSearchService {

    private final TicketRepository ticketRepository;
    private final KBRepository kbRepository;
    private final AIService aiService;

    public AdvancedSearchService() {
        this.ticketRepository = new TicketRepository();
        this.kbRepository = new KBRepository();
        this.aiService = new AIService();
    }

    /**
     * Perform semantic search across tickets
     */
    public List<Ticket> semanticTicketSearch(String query) {
        List<Ticket> allTickets = ticketRepository.findAll();
        List<String> queryKeywords = aiService.extractKeywords(query);

        // Score tickets based on keyword relevance
        Map<Ticket, Double> scoredTickets = new HashMap<>();

        for (Ticket ticket : allTickets) {
            double score = calculateRelevanceScore(ticket, queryKeywords);
            if (score > 0.3) { // Threshold
                scoredTickets.put(ticket, score);
            }
        }

        // Sort by score descending
        return scoredTickets.entrySet().stream()
                .sorted(Map.Entry.<Ticket, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * Advanced filter search with multiple criteria
     */
    public List<Ticket> advancedSearch(SearchCriteria criteria) {
        List<Ticket> results = ticketRepository.findAll();

        // Apply filters
        if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
            results = results.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(criteria.getStatus()))
                    .collect(Collectors.toList());
        }

        if (criteria.getPriority() != null && !criteria.getPriority().isEmpty()) {
            results = results.stream()
                    .filter(t -> t.getPriority().equalsIgnoreCase(criteria.getPriority()))
                    .collect(Collectors.toList());
        }

        if (criteria.getCategory() != null && !criteria.getCategory().isEmpty()) {
            results = results.stream()
                    .filter(t -> t.getCategory() != null &&
                            t.getCategory().equalsIgnoreCase(criteria.getCategory()))
                    .collect(Collectors.toList());
        }

        if (criteria.getAssignedTo() != null && !criteria.getAssignedTo().isEmpty()) {
            results = results.stream()
                    .filter(t -> t.getAssignedTo() != null &&
                            t.getAssignedTo().equalsIgnoreCase(criteria.getAssignedTo()))
                    .collect(Collectors.toList());
        }

        if (criteria.getSearchText() != null && !criteria.getSearchText().isEmpty()) {
            String searchLower = criteria.getSearchText().toLowerCase();
            results = results.stream()
                    .filter(t -> matchesSearchText(t, searchLower))
                    .collect(Collectors.toList());
        }

        if (criteria.getCreatedAfter() != null) {
            results = results.stream()
                    .filter(t -> t.getCreatedAt() != null &&
                            t.getCreatedAt().isAfter(criteria.getCreatedAfter()))
                    .collect(Collectors.toList());
        }

        if (criteria.getCreatedBefore() != null) {
            results = results.stream()
                    .filter(t -> t.getCreatedAt() != null &&
                            t.getCreatedAt().isBefore(criteria.getCreatedBefore()))
                    .collect(Collectors.toList());
        }

        // Apply sorting
        if (criteria.getSortBy() != null) {
            results = sortTickets(results, criteria.getSortBy(), criteria.isSortDescending());
        }

        return results;
    }

    /**
     * Search for similar tickets using AI
     */
    public List<Ticket> findSimilarTickets(Ticket ticket, int limit) {
        List<Ticket> allTickets = ticketRepository.findAll();
        Map<Ticket, Double> similarities = new HashMap<>();

        for (Ticket other : allTickets) {
            if (!other.getId().equals(ticket.getId())) {
                double similarity = aiService.calculateSimilarity(ticket, other);
                if (similarity > 0.4) {
                    similarities.put(other, similarity);
                }
            }
        }

        return similarities.entrySet().stream()
                .sorted(Map.Entry.<Ticket, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Search knowledge base with semantic understanding
     */
    public List<KnowledgeBase> searchKnowledgeBase(String query) {
        List<KnowledgeBase> articles = kbRepository.findAll();
        List<String> queryKeywords = aiService.extractKeywords(query);

        Map<KnowledgeBase, Double> scoredArticles = new HashMap<>();

        for (KnowledgeBase article : articles) {
            if (!article.isPublished()) continue;

            double score = calculateArticleRelevance(article, queryKeywords);
            if (score > 0.3) {
                scoredArticles.put(article, score);
            }
        }

        return scoredArticles.entrySet().stream()
                .sorted(Map.Entry.<KnowledgeBase, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Get search suggestions based on partial query
     */
    public List<String> getSearchSuggestions(String partialQuery) {
        if (partialQuery == null || partialQuery.length() < 2) {
            return Collections.emptyList();
        }

        List<Ticket> recentTickets = ticketRepository.findAll().stream()
                .limit(100)
                .collect(Collectors.toList());

        Set<String> suggestions = new HashSet<>();
        String lowerQuery = partialQuery.toLowerCase();

        // Extract common terms from recent tickets
        for (Ticket ticket : recentTickets) {
            String[] titleWords = ticket.getTitle().toLowerCase().split("\\s+");
            for (String word : titleWords) {
                if (word.startsWith(lowerQuery) && word.length() > 3) {
                    suggestions.add(word);
                }
            }
        }

        return suggestions.stream()
                .sorted()
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Global search across all entities
     */
    public Map<String, List<?>> globalSearch(String query) {
        Map<String, List<?>> results = new HashMap<>();

        results.put("tickets", semanticTicketSearch(query));
        results.put("knowledgeBase", searchKnowledgeBase(query));

        return results;
    }

    // Helper Methods

    private double calculateRelevanceScore(Ticket ticket, List<String> keywords) {
        String searchableText = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();
        double score = 0.0;

        for (String keyword : keywords) {
            if (searchableText.contains(keyword.toLowerCase())) {
                score += 1.0;
            }
        }

        // Boost score for title matches
        String titleLower = ticket.getTitle().toLowerCase();
        for (String keyword : keywords) {
            if (titleLower.contains(keyword.toLowerCase())) {
                score += 0.5;
            }
        }

        return score / keywords.size();
    }

    private double calculateArticleRelevance(KnowledgeBase article, List<String> keywords) {
        String searchableText = (article.getTitle() + " " + article.getContent()).toLowerCase();
        double score = 0.0;

        for (String keyword : keywords) {
            if (searchableText.contains(keyword.toLowerCase())) {
                score += 1.0;
            }
        }

        // Boost for title matches
        if (article.getTitle() != null) {
            String titleLower = article.getTitle().toLowerCase();
            for (String keyword : keywords) {
                if (titleLower.contains(keyword.toLowerCase())) {
                    score += 0.5;
                }
            }
        }

        return score / keywords.size();
    }

    private boolean matchesSearchText(Ticket ticket, String searchText) {
        return (ticket.getTitle() != null && ticket.getTitle().toLowerCase().contains(searchText)) ||
                (ticket.getDescription() != null && ticket.getDescription().toLowerCase().contains(searchText)) ||
                (ticket.getId() != null && ticket.getId().toLowerCase().contains(searchText));
    }

    private List<Ticket> sortTickets(List<Ticket> tickets, String sortBy, boolean descending) {
        Comparator<Ticket> comparator;

        switch (sortBy.toLowerCase()) {
            case "created":
                comparator = Comparator.comparing(Ticket::getCreatedAt);
                break;
            case "updated":
                comparator = Comparator.comparing(Ticket::getUpdatedAt);
                break;
            case "priority":
                comparator = Comparator.comparing(t -> getPriorityValue(t.getPriority()));
                break;
            case "status":
                comparator = Comparator.comparing(Ticket::getStatus);
                break;
            default:
                comparator = Comparator.comparing(Ticket::getCreatedAt);
        }

        if (descending) {
            comparator = comparator.reversed();
        }

        return tickets.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private int getPriorityValue(String priority) {
        switch (priority.toUpperCase()) {
            case "CRITICAL": return 4;
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    // Inner class for search criteria
    public static class SearchCriteria {
        private String status;
        private String priority;
        private String category;
        private String assignedTo;
        private String searchText;
        private java.time.LocalDateTime createdAfter;
        private java.time.LocalDateTime createdBefore;
        private String sortBy;
        private boolean sortDescending;

        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getAssignedTo() { return assignedTo; }
        public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }

        public String getSearchText() { return searchText; }
        public void setSearchText(String searchText) { this.searchText = searchText; }

        public java.time.LocalDateTime getCreatedAfter() { return createdAfter; }
        public void setCreatedAfter(java.time.LocalDateTime createdAfter) {
            this.createdAfter = createdAfter;
        }

        public java.time.LocalDateTime getCreatedBefore() { return createdBefore; }
        public void setCreatedBefore(java.time.LocalDateTime createdBefore) {
            this.createdBefore = createdBefore;
        }

        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }

        public boolean isSortDescending() { return sortDescending; }
        public void setSortDescending(boolean sortDescending) {
            this.sortDescending = sortDescending;
        }
    }
}