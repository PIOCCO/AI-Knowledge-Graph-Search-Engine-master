package org.example.repository;

import org.example.model.KnowledgeBase;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class KBRepository {
    private final Driver driver;

    public KBRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public KnowledgeBase save(KnowledgeBase kb) {
        try (Session session = driver.session()) {
            String query = "MERGE (k:KnowledgeBase {id: $id}) " +
                    "SET k.title = $title, k.content = $content, k.categoryId = $categoryId, " +
                    "k.authorId = $authorId, k.viewCount = $viewCount, k.helpfulCount = $helpfulCount, " +
                    "k.published = $published, k.createdAt = $createdAt RETURN k";

            session.run(query, parameters(
                    "id", kb.getId(),
                    "title", kb.getTitle(),
                    "content", kb.getContent(),
                    "categoryId", kb.getCategoryId(),
                    "authorId", kb.getAuthorId(),
                    "viewCount", kb.getViewCount(),
                    "helpfulCount", kb.getHelpfulCount(),
                    "published", kb.isPublished(),
                    "createdAt",
                    kb.getCreatedAt() != null ? kb.getCreatedAt().toString() : LocalDateTime.now().toString()));
            return kb;
        }
    }

    public KnowledgeBase findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (k:KnowledgeBase {id: $id}) RETURN k";
            Result result = session.run(query, parameters("id", id));
            if (result.hasNext()) {
                return mapToKB(result.next().get("k").asMap());
            }
            return null;
        }
    }

    public List<KnowledgeBase> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (k:KnowledgeBase {published: true}) RETURN k ORDER BY k.createdAt DESC";
            Result result = session.run(query);
            List<KnowledgeBase> articles = new ArrayList<>();
            while (result.hasNext()) {
                articles.add(mapToKB(result.next().get("k").asMap()));
            }
            return articles;
        }
    }

    public List<KnowledgeBase> findByCategory(String categoryId) {
        try (Session session = driver.session()) {
            String query = "MATCH (k:KnowledgeBase {categoryId: $categoryId, published: true}) RETURN k ORDER BY k.viewCount DESC";
            Result result = session.run(query, parameters("categoryId", categoryId));
            List<KnowledgeBase> articles = new ArrayList<>();
            while (result.hasNext()) {
                articles.add(mapToKB(result.next().get("k").asMap()));
            }
            return articles;
        }
    }

    public List<KnowledgeBase> searchByTitle(String searchTerm) {
        try (Session session = driver.session()) {
            String query = "MATCH (k:KnowledgeBase) WHERE k.title CONTAINS $searchTerm AND k.published = true RETURN k ORDER BY k.viewCount DESC LIMIT 20";
            Result result = session.run(query, parameters("searchTerm", searchTerm));
            List<KnowledgeBase> articles = new ArrayList<>();
            while (result.hasNext()) {
                articles.add(mapToKB(result.next().get("k").asMap()));
            }
            return articles;
        }
    }

    public void delete(String id) {
        try (Session session = driver.session()) {
            session.run("MATCH (k:KnowledgeBase {id: $id}) DETACH DELETE k", parameters("id", id));
        }
    }

    private KnowledgeBase mapToKB(Map<String, Object> map) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId((String) map.get("id"));
        kb.setTitle((String) map.get("title"));
        kb.setContent((String) map.get("content"));
        kb.setCategoryId((String) map.get("categoryId"));
        kb.setAuthorId((String) map.get("authorId"));
        kb.setViewCount(map.get("viewCount") != null ? ((Long) map.get("viewCount")).intValue() : 0);
        kb.setHelpfulCount(map.get("helpfulCount") != null ? ((Long) map.get("helpfulCount")).intValue() : 0);
        kb.setPublished(map.get("published") != null ? (Boolean) map.get("published") : false);
        if (map.get("createdAt") != null) {
            kb.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return kb;
    }
}
