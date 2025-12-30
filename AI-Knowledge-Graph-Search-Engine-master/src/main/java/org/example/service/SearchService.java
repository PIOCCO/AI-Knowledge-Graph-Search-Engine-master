package org.example.service;

import org.example.model.Ticket;
import org.example.model.KnowledgeBase;
import org.example.model.User;
import org.example.repository.TicketRepository;
import org.example.repository.KBRepository;
import org.example.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private final TicketRepository ticketRepository;
    private final KBRepository kbRepository;
    private final UserRepository userRepository;

    public SearchService() {
        this.ticketRepository = new TicketRepository();
        this.kbRepository = new KBRepository();
        this.userRepository = new UserRepository();
    }

    public List<Ticket> searchTickets(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return ticketRepository.search(query.trim());
    }

    public List<Ticket> advancedTicketSearch(Map<String, Object> criteria) {
        List<Ticket> allTickets = ticketRepository.findAll();

        return allTickets.stream()
                .filter(ticket -> matchesCriteria(ticket, criteria))
                .collect(Collectors.toList());
    }

    private boolean matchesCriteria(Ticket ticket, Map<String, Object> criteria) {
        // Status filter
        if (criteria.containsKey("status")) {
            String status = (String) criteria.get("status");
            if (!ticket.getStatus().equals(status)) {
                return false;
            }
        }

        // Priority filter
        if (criteria.containsKey("priority")) {
            String priority = (String) criteria.get("priority");
            if (!ticket.getPriority().equals(priority)) {
                return false;
            }
        }

        // Assignee filter
        if (criteria.containsKey("assignedTo")) {
            String assignedTo = (String) criteria.get("assignedTo");
            if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().equals(assignedTo)) {
                return false;
            }
        }

        // Text search in title/description
        if (criteria.containsKey("text")) {
            String text = ((String) criteria.get("text")).toLowerCase();
            String searchableText = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();
            if (!searchableText.contains(text)) {
                return false;
            }
        }

        return true;
    }

    public List<KnowledgeBase> searchKnowledgeBase(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return kbRepository.searchByTitle(query.trim());
    }

    public List<KnowledgeBase> searchKnowledgeBaseByCategory(String categoryId) {
        return kbRepository.findByCategory(categoryId);
    }

    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<User> allUsers = userRepository.findAll();
        String lowerQuery = query.toLowerCase();

        return allUsers.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(lowerQuery) ||
                        user.getFullName().toLowerCase().contains(lowerQuery) ||
                        user.getEmail().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    public Map<String, Object> globalSearch(String query) {
        Map<String, Object> results = new HashMap<>();

        if (query == null || query.trim().isEmpty()) {
            results.put("tickets", new ArrayList<>());
            results.put("knowledgeBase", new ArrayList<>());
            results.put("users", new ArrayList<>());
            return results;
        }

        results.put("tickets", searchTickets(query));
        results.put("knowledgeBase", searchKnowledgeBase(query));
        results.put("users", searchUsers(query));
        results.put("query", query);
        results.put("totalResults",
                ((List<?>) results.get("tickets")).size() +
                        ((List<?>) results.get("knowledgeBase")).size() +
                        ((List<?>) results.get("users")).size());

        return results;
    }

    public List<Ticket> searchTicketsByDateRange(String startDate, String endDate) {
        // Placeholder - would need date-based repository query
        return new ArrayList<>();
    }

    public List<Ticket> searchSimilarTickets(Ticket ticket) {
        List<Ticket> allTickets = ticketRepository.findAll();
        AIService aiService = new AIService();

        // Find tickets with similarity > 0.5
        return allTickets.stream()
                .filter(t -> !t.getId().equals(ticket.getId()))
                .filter(t -> aiService.calculateSimilarity(ticket, t) > 0.5)
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<String> getSuggestedSearchTerms(String partialQuery) {
        if (partialQuery == null || partialQuery.length() < 2) {
            return new ArrayList<>();
        }

        // Simple suggestion based on common terms
        List<String> suggestions = Arrays.asList(
                "bug", "feature", "error", "crash", "performance",
                "login", "password", "network", "database", "security");

        String lowerQuery = partialQuery.toLowerCase();
        return suggestions.stream()
                .filter(term -> term.startsWith(lowerQuery))
                .limit(5)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getSearchStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalTickets", ticketRepository.count());
        stats.put("totalKBArticles", (long) kbRepository.findAll().size());
        stats.put("totalUsers", (long) userRepository.findAll().size());
        return stats;
    }

    public List<Ticket> filterTicketsByTags(List<String> tags) {
        // Placeholder - would need tag-based filtering
        return new ArrayList<>();
    }

    public List<KnowledgeBase> getRelatedArticles(Ticket ticket) {
        // Find KB articles related to ticket category or keywords
        AIService aiService = new AIService();
        List<String> keywords = aiService.extractKeywords(ticket.getTitle() + " " + ticket.getDescription());

        List<KnowledgeBase> allArticles = kbRepository.findAll();

        return allArticles.stream()
                .filter(article -> {
                    String articleText = (article.getTitle() + " " + article.getContent()).toLowerCase();
                    return keywords.stream().anyMatch(keyword -> articleText.contains(keyword.toLowerCase()));
                })
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<Ticket> searchByCustomField(String fieldName, String fieldValue) {
        // Placeholder for custom field search
        return new ArrayList<>();
    }

    public Map<String, Object> getFacetedSearchResults(String query) {
        List<Ticket> tickets = searchTickets(query);

        Map<String, Object> facets = new HashMap<>();

        // Status facets
        Map<String, Long> statusFacets = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));
        facets.put("status", statusFacets);

        // Priority facets
        Map<String, Long> priorityFacets = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
        facets.put("priority", priorityFacets);

        facets.put("results", tickets);
        facets.put("totalCount", tickets.size());

        return facets;
    }
}
