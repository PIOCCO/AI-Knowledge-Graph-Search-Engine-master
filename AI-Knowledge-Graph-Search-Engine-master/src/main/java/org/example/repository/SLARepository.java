package org.example.repository;

import org.example.model.SLA;
import org.example.model.enums.Priority;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class SLARepository {
    private final Driver driver;

    public SLARepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public SLA save(SLA sla) {
        try (Session session = driver.session()) {
            String query = "MERGE (s:SLA {id: $id}) " +
                    "SET s.name = $name, s.description = $description, s.priority = $priority, " +
                    "s.responseTimeMinutes = $responseTimeMinutes, s.resolutionTimeMinutes = $resolutionTimeMinutes, " +
                    "s.active = $active, s.createdAt = $createdAt, s.categoryId = $categoryId RETURN s";

            session.run(query, parameters(
                    "id", sla.getId(),
                    "name", sla.getName(),
                    "description", sla.getDescription(),
                    "priority", sla.getPriority() != null ? sla.getPriority().name() : null,
                    "responseTimeMinutes", sla.getResponseTimeMinutes(),
                    "resolutionTimeMinutes", sla.getResolutionTimeMinutes(),
                    "active", sla.isActive(),
                    "createdAt",
                    sla.getCreatedAt() != null ? sla.getCreatedAt().toString() : LocalDateTime.now().toString(),
                    "categoryId", sla.getCategoryId()));
            return sla;
        }
    }

    public SLA findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (s:SLA {id: $id}) RETURN s";
            Result result = session.run(query, parameters("id", id));
            if (result.hasNext()) {
                return mapToSLA(result.next().get("s").asMap());
            }
            return null;
        }
    }

    public List<SLA> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (s:SLA) RETURN s ORDER BY s.priority, s.name";
            Result result = session.run(query);
            List<SLA> slas = new ArrayList<>();
            while (result.hasNext()) {
                slas.add(mapToSLA(result.next().get("s").asMap()));
            }
            return slas;
        }
    }

    public SLA findByPriority(Priority priority) {
        try (Session session = driver.session()) {
            String query = "MATCH (s:SLA {priority: $priority, active: true}) RETURN s LIMIT 1";
            Result result = session.run(query, parameters("priority", priority.name()));
            if (result.hasNext()) {
                return mapToSLA(result.next().get("s").asMap());
            }
            return null;
        }
    }

    public void delete(String id) {
        try (Session session = driver.session()) {
            session.run("MATCH (s:SLA {id: $id}) DETACH DELETE s", parameters("id", id));
        }
    }

    private SLA mapToSLA(Map<String, Object> map) {
        SLA sla = new SLA();
        sla.setId((String) map.get("id"));
        sla.setName((String) map.get("name"));
        sla.setDescription((String) map.get("description"));
        if (map.get("priority") != null) {
            sla.setPriority(Priority.valueOf((String) map.get("priority")));
        }
        sla.setResponseTimeMinutes(
                map.get("responseTimeMinutes") != null ? ((Long) map.get("responseTimeMinutes")).intValue() : 0);
        sla.setResolutionTimeMinutes(
                map.get("resolutionTimeMinutes") != null ? ((Long) map.get("resolutionTimeMinutes")).intValue() : 0);
        sla.setActive(map.get("active") != null ? (Boolean) map.get("active") : true);
        sla.setCategoryId((String) map.get("categoryId"));
        if (map.get("createdAt") != null) {
            sla.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return sla;
    }
}
