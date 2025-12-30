package org.example.repository;

import org.example.model.Category;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class CategoryRepository {
    private final Driver driver;

    public CategoryRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public Category save(Category category) {
        try (Session session = driver.session()) {
            String query = "MERGE (c:Category {id: $id}) " +
                    "SET c.name = $name, c.description = $description, c.parentCategoryId = $parentCategoryId, " +
                    "c.color = $color, c.icon = $icon, c.active = $active, c.createdAt = $createdAt, " +
                    "c.ticketCount = $ticketCount RETURN c";

            session.run(query, parameters(
                    "id", category.getId(),
                    "name", category.getName(),
                    "description", category.getDescription(),
                    "parentCategoryId", category.getParentCategoryId(),
                    "color", category.getColor(),
                    "icon", category.getIcon(),
                    "active", category.isActive(),
                    "createdAt",
                    category.getCreatedAt() != null ? category.getCreatedAt().toString()
                            : LocalDateTime.now().toString(),
                    "ticketCount", category.getTicketCount()));
            return category;
        }
    }

    public Category findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Category {id: $id}) RETURN c";
            Result result = session.run(query, parameters("id", id));
            if (result.hasNext()) {
                return mapToCategory(result.next().get("c").asMap());
            }
            return null;
        }
    }

    public List<Category> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Category) RETURN c ORDER BY c.name";
            Result result = session.run(query);
            List<Category> categories = new ArrayList<>();
            while (result.hasNext()) {
                categories.add(mapToCategory(result.next().get("c").asMap()));
            }
            return categories;
        }
    }

    public List<Category> findByParentId(String parentId) {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Category {parentCategoryId: $parentId}) RETURN c ORDER BY c.name";
            Result result = session.run(query, parameters("parentId", parentId));
            List<Category> categories = new ArrayList<>();
            while (result.hasNext()) {
                categories.add(mapToCategory(result.next().get("c").asMap()));
            }
            return categories;
        }
    }

    public List<Category> findRootCategories() {
        try (Session session = driver.session()) {
            String query = "MATCH (c:Category) WHERE c.parentCategoryId IS NULL RETURN c ORDER BY c.name";
            Result result = session.run(query);
            List<Category> categories = new ArrayList<>();
            while (result.hasNext()) {
                categories.add(mapToCategory(result.next().get("c").asMap()));
            }
            return categories;
        }
    }

    public void delete(String id) {
        try (Session session = driver.session()) {
            session.run("MATCH (c:Category {id: $id}) DETACH DELETE c", parameters("id", id));
        }
    }

    private Category mapToCategory(Map<String, Object> map) {
        Category category = new Category();
        category.setId((String) map.get("id"));
        category.setName((String) map.get("name"));
        category.setDescription((String) map.get("description"));
        category.setParentCategoryId((String) map.get("parentCategoryId"));
        category.setColor((String) map.get("color"));
        category.setIcon((String) map.get("icon"));
        category.setActive(map.get("active") != null ? (Boolean) map.get("active") : true);
        category.setTicketCount(map.get("ticketCount") != null ? ((Long) map.get("ticketCount")).intValue() : 0);
        if (map.get("createdAt") != null) {
            category.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return category;
    }
}
