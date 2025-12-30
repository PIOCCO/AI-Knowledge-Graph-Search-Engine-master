package org.example.service;

import org.example.model.Ticket;
import org.example.model.KnowledgeBase;
import org.example.model.enums.Priority;
import org.example.model.enums.Severity;

import java.util.*;

public class AIService {

    public AIService() {
    }

    public String classifyTicket(String text) {
        return classifyTicket(text, "");
    }

    public String classifyTicket(String title, String description) {
        String text = (title + " " + description).toLowerCase();

        // Simple keyword-based classification
        if (text.contains("bug") || text.contains("error") || text.contains("crash")) {
            return "BUG";
        } else if (text.contains("feature") || text.contains("enhancement") || text.contains("improve")) {
            return "FEATURE_REQUEST";
        } else if (text.contains("question") || text.contains("how to") || text.contains("help")) {
            return "QUESTION";
        } else if (text.contains("urgent") || text.contains("critical") || text.contains("down")) {
            return "INCIDENT";
        }

        return "GENERAL";
    }

    public Priority suggestPriority(String text) {
        return suggestPriority(text, "");
    }

    public Priority suggestPriority(String title, String description) {
        String text = (title + " " + description).toLowerCase();

        // Priority keywords
        if (text.contains("critical") || text.contains("urgent") || text.contains("production down") ||
                text.contains("security") || text.contains("data loss")) {
            return Priority.CRITICAL;
        } else if (text.contains("high") || text.contains("important") || text.contains("asap") ||
                text.contains("blocking")) {
            return Priority.HIGH;
        } else if (text.contains("low") || text.contains("minor") || text.contains("cosmetic")) {
            return Priority.LOW;
        }

        return Priority.MEDIUM;
    }

    public Severity suggestSeverity(String title, String description) {
        String text = (title + " " + description).toLowerCase();

        if (text.contains("blocker") || text.contains("cannot") || text.contains("unable to")) {
            return Severity.BLOCKER;
        } else if (text.contains("major") || text.contains("significant") || text.contains("important")) {
            return Severity.MAJOR;
        } else if (text.contains("minor") || text.contains("small") || text.contains("cosmetic")) {
            return Severity.MINOR;
        }

        return Severity.LOW;
    }

    public String suggestAssignee(Ticket ticket) {
        // Simple rule-based assignment
        String category = ticket.getCategoryId();

        if (category != null) {
            if (category.contains("network")) {
                return "network-team";
            } else if (category.contains("database")) {
                return "database-team";
            } else if (category.contains("frontend")) {
                return "frontend-team";
            } else if (category.contains("backend")) {
                return "backend-team";
            }
        }

        // Default to general support
        return "support-team";
    }

    public List<KnowledgeBase> suggestSolutions(Ticket ticket) {
        // In a real implementation, this would use ML/NLP to find similar resolved
        // tickets
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }

    public double calculateSimilarity(Ticket ticket1, Ticket ticket2) {
        // Simple word-based similarity
        Set<String> words1 = extractWords(ticket1.getTitle() + " " + ticket1.getDescription());
        Set<String> words2 = extractWords(ticket2.getTitle() + " " + ticket2.getDescription());

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        if (union.isEmpty())
            return 0.0;
        return (double) intersection.size() / union.size();
    }

    private Set<String> extractWords(String text) {
        String[] words = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+");

        Set<String> wordSet = new HashSet<>();
        for (String word : words) {
            if (word.length() > 3) { // Filter out short words
                wordSet.add(word);
            }
        }
        return wordSet;
    }

    public List<String> extractKeywords(String text) {
        Map<String, Integer> wordFrequency = new HashMap<>();
        String[] words = text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+");

        for (String word : words) {
            if (word.length() > 4) { // Only consider longer words
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }

        // Sort by frequency and return top keywords
        return wordFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }

    public String generateSummary(String text) {
        // Simple summary: first 200 characters
        if (text.length() <= 200) {
            return text;
        }
        return text.substring(0, 197) + "...";
    }

    public Map<String, Object> analyzeTicket(Ticket ticket) {
        Map<String, Object> analysis = new HashMap<>();

        analysis.put("classification", classifyTicket(ticket.getTitle(), ticket.getDescription()));
        analysis.put("suggestedPriority", suggestPriority(ticket.getTitle(), ticket.getDescription()));
        analysis.put("suggestedSeverity", suggestSeverity(ticket.getTitle(), ticket.getDescription()));
        analysis.put("suggestedAssignee", suggestAssignee(ticket));
        analysis.put("keywords", extractKeywords(ticket.getTitle() + " " + ticket.getDescription()));
        analysis.put("summary", generateSummary(ticket.getDescription()));

        return analysis;
    }
}
