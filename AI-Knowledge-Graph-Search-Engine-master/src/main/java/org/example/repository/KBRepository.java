package org.example.repository;

import org.example.model.KnowledgeBase;
import org.neo4j.driver.*;

import java.time.LocalDateTime;
import java.util.*;
import org.neo4j.driver.Record;


import static org.neo4j.driver.Values.parameters;

/**
 * Knowledge Base Repository - KB articles data access
 */
public class KBRepository {

    private final Driver driver;

    public KBRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    /**
     * Save or update KB article
     */
    public void save(KnowledgeBase article) {
        try (Session session = driver.session()) {
            String query = "MERGE (kb:KnowledgeBase {id: $id}) " +
                    "SET kb.title = $title, " +
                    "kb.content = $content, " +
                    "kb.category = $category, " +
                    "kb.tags = $tags, " +
                    "kb.authorId = $authorId, " +
                    "kb.authorName = $authorName, " +
                    "kb.published = $published, " +
                    "kb.viewCount = $viewCount, " +
                    "kb.helpfulCount = $helpfulCount, " +
                    "kb.icon = $icon, " +
                    "kb.createdAt = $createdAt, " +
                    "kb.updatedAt = $updatedAt, " +
                    "kb.publishedAt = $publishedAt";

            session.run(query, parameters(
                    "id", article.getId(),
                    "title", article.getTitle(),
                    "content", article.getContent(),
                    "category", article.getCategory(),
                    "tags", article.getTags(),
                    "authorId", article.getAuthorId(),
                    "authorName", article.getAuthorName(),
                    "published", article.isPublished(),
                    "viewCount", article.getViewCount(),
                    "helpfulCount", article.getHelpfulCount(),
                    "icon", article.getIcon(),
                    "createdAt", article.getCreatedAt() != null ? article.getCreatedAt().toString() : null,
                    "updatedAt", article.getUpdatedAt() != null ? article.getUpdatedAt().toString() : null,
                    "publishedAt", article.getPublishedAt() != null ? article.getPublishedAt().toString() : null
            ));

            System.out.println("✅ KB article saved: " + article.getTitle());
        } catch (Exception e) {
            System.err.println("❌ Error saving KB article: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Find all KB articles
     */
    public List<KnowledgeBase> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase) RETURN kb ORDER BY kb.createdAt DESC";

            Result result = session.run(query);
            List<KnowledgeBase> articles = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                articles.add(mapToKnowledgeBase(record.get("kb").asMap()));
            }

            return articles;
        } catch (Exception e) {
            System.err.println("❌ Error finding KB articles: " + e.getMessage());
            return createSampleArticles(); // Return sample data if DB fails
        }
    }

    /**
     * Find KB article by ID
     */
    public KnowledgeBase findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {id: $id}) RETURN kb";

            Result result = session.run(query, parameters("id", id));

            if (result.hasNext()) {
                return mapToKnowledgeBase(result.next().get("kb").asMap());
            }

            return null;
        } catch (Exception e) {
            System.err.println("❌ Error finding KB article: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find published articles
     */
    public List<KnowledgeBase> findPublished() {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {published: true}) " +
                    "RETURN kb ORDER BY kb.viewCount DESC";

            Result result = session.run(query);
            List<KnowledgeBase> articles = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                articles.add(mapToKnowledgeBase(record.get("kb").asMap()));
            }

            return articles;
        } catch (Exception e) {
            System.err.println("❌ Error finding published articles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Find articles by category
     */
    public List<KnowledgeBase> findByCategory(String category) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {category: $category}) " +
                    "RETURN kb ORDER BY kb.createdAt DESC";

            Result result = session.run(query, parameters("category", category));
            List<KnowledgeBase> articles = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                articles.add(mapToKnowledgeBase(record.get("kb").asMap()));
            }

            return articles;
        } catch (Exception e) {
            System.err.println("❌ Error finding articles by category: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Search articles by keyword
     */
    public List<KnowledgeBase> search(String keyword) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase) " +
                    "WHERE kb.title CONTAINS $keyword OR kb.content CONTAINS $keyword " +
                    "RETURN kb ORDER BY kb.viewCount DESC";

            Result result = session.run(query, parameters("keyword", keyword));
            List<KnowledgeBase> articles = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                articles.add(mapToKnowledgeBase(record.get("kb").asMap()));
            }

            return articles;
        } catch (Exception e) {
            System.err.println("❌ Error searching KB articles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Delete KB article
     */
    public void delete(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {id: $id}) DELETE kb";
            session.run(query, parameters("id", id));
            System.out.println("✅ KB article deleted: " + id);
        } catch (Exception e) {
            System.err.println("❌ Error deleting KB article: " + e.getMessage());
        }
    }

    /**
     * Increment view count
     */
    public void incrementViewCount(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {id: $id}) " +
                    "SET kb.viewCount = kb.viewCount + 1";
            session.run(query, parameters("id", id));
        } catch (Exception e) {
            System.err.println("❌ Error incrementing view count: " + e.getMessage());
        }
    }

    /**
     * Increment helpful count
     */
    public void incrementHelpfulCount(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {id: $id}) " +
                    "SET kb.helpfulCount = kb.helpfulCount + 1";
            session.run(query, parameters("id", id));
        } catch (Exception e) {
            System.err.println("❌ Error incrementing helpful count: " + e.getMessage());
        }
    }

    /**
     * Get popular articles
     */
    public List<KnowledgeBase> findPopular(int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (kb:KnowledgeBase {published: true}) " +
                    "RETURN kb ORDER BY kb.viewCount DESC LIMIT $limit";

            Result result = session.run(query, parameters("limit", limit));
            List<KnowledgeBase> articles = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                articles.add(mapToKnowledgeBase(record.get("kb").asMap()));
            }

            return articles;
        } catch (Exception e) {
            System.err.println("❌ Error finding popular articles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Private helper methods

    private KnowledgeBase mapToKnowledgeBase(Map<String, Object> map) {
        KnowledgeBase article = new KnowledgeBase();

        article.setId((String) map.get("id"));
        article.setTitle((String) map.get("title"));
        article.setContent((String) map.get("content"));
        article.setCategory((String) map.get("category"));
        article.setAuthorId((String) map.get("authorId"));
        article.setAuthorName((String) map.get("authorName"));
        article.setIcon((String) map.get("icon"));

        if (map.get("published") != null) {
            article.setPublished((Boolean) map.get("published"));
        }

        if (map.get("viewCount") != null) {
            article.setViewCount(((Long) map.get("viewCount")).intValue());
        }

        if (map.get("helpfulCount") != null) {
            article.setHelpfulCount(((Long) map.get("helpfulCount")).intValue());
        }

        if (map.get("tags") != null) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) map.get("tags");
            article.setTags(tags);
        }

        if (map.get("createdAt") != null) {
            article.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }

        if (map.get("updatedAt") != null) {
            article.setUpdatedAt(LocalDateTime.parse((String) map.get("updatedAt")));
        }

        if (map.get("publishedAt") != null) {
            article.setPublishedAt(LocalDateTime.parse((String) map.get("publishedAt")));
        }

        return article;
    }

    /**
     * Create sample KB articles for demo
     */
    private List<KnowledgeBase> createSampleArticles() {
        List<KnowledgeBase> articles = new ArrayList<>();

        KnowledgeBase kb1 = new KnowledgeBase(
                "Getting Started with Ticket Management",
                "Learn how to create and manage tickets effectively...",
                "Getting Started"
        );
        kb1.setId("KB-001");
        kb1.setPublished(true);
        kb1.setAuthorName("Admin");
        kb1.addTag("tutorial");
        kb1.addTag("basics");
        articles.add(kb1);

        KnowledgeBase kb2 = new KnowledgeBase(
                "How to Create a New Ticket",
                "Step-by-step guide to creating tickets...",
                "How-To Guides"
        );
        kb2.setId("KB-002");
        kb2.setPublished(true);
        kb2.setAuthorName("Admin");
        kb2.addTag("tutorial");
        kb2.addTag("tickets");
        articles.add(kb2);

        KnowledgeBase kb3 = new KnowledgeBase(
                "Troubleshooting Connection Issues",
                "Common connection problems and solutions...",
                "Troubleshooting"
        );
        kb3.setId("KB-003");
        kb3.setPublished(true);
        kb3.setAuthorName("Support Team");
        kb3.addTag("troubleshooting");
        kb3.addTag("connection");
        articles.add(kb3);

        KnowledgeBase kb4 = new KnowledgeBase(
                "API Documentation Overview",
                "Complete API reference and examples...",
                "Reference"
        );
        kb4.setId("KB-004");
        kb4.setPublished(true);
        kb4.setAuthorName("Dev Team");
        kb4.addTag("api");
        kb4.addTag("reference");
        articles.add(kb4);

        KnowledgeBase kb5 = new KnowledgeBase(
                "Managing Categories and Tags",
                "Organize your tickets with categories...",
                "How-To Guides"
        );
        kb5.setId("KB-005");
        kb5.setPublished(false);
        kb5.setAuthorName("Admin");
        kb5.addTag("categories");
        kb5.addTag("organization");
        articles.add(kb5);

        return articles;
    }
}