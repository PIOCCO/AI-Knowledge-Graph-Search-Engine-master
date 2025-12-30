package org.example.repository;

import org.example.model.Metric;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class MetricRepository {
    private final Driver driver;

    public MetricRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public Metric save(Metric metric) {
        try (Session session = driver.session()) {
            String query = "CREATE (m:Metric {id: $id, name: $name, description: $description, " +
                    "metricType: $metricType, value: $value, unit: $unit, timestamp: $timestamp, " +
                    "entityId: $entityId, entityType: $entityType, period: $period}) RETURN m";

            session.run(query, parameters(
                    "id", metric.getId(),
                    "name", metric.getName(),
                    "description", metric.getDescription(),
                    "metricType", metric.getMetricType(),
                    "value", metric.getValue(),
                    "unit", metric.getUnit(),
                    "timestamp",
                    metric.getTimestamp() != null ? metric.getTimestamp().toString() : LocalDateTime.now().toString(),
                    "entityId", metric.getEntityId(),
                    "entityType", metric.getEntityType(),
                    "period", metric.getPeriod()));
            return metric;
        }
    }

    public List<Metric> findByEntityId(String entityId) {
        try (Session session = driver.session()) {
            String query = "MATCH (m:Metric {entityId: $entityId}) RETURN m ORDER BY m.timestamp DESC";
            Result result = session.run(query, parameters("entityId", entityId));
            List<Metric> metrics = new ArrayList<>();
            while (result.hasNext()) {
                metrics.add(mapToMetric(result.next().get("m").asMap()));
            }
            return metrics;
        }
    }

    public List<Metric> findByMetricType(String metricType, LocalDateTime startDate, LocalDateTime endDate) {
        try (Session session = driver.session()) {
            String query = "MATCH (m:Metric {metricType: $metricType}) " +
                    "WHERE m.timestamp >= $startDate AND m.timestamp <= $endDate " +
                    "RETURN m ORDER BY m.timestamp";
            Result result = session.run(query, parameters(
                    "metricType", metricType,
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString()));
            List<Metric> metrics = new ArrayList<>();
            while (result.hasNext()) {
                metrics.add(mapToMetric(result.next().get("m").asMap()));
            }
            return metrics;
        }
    }

    public List<Metric> findRecent(int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (m:Metric) RETURN m ORDER BY m.timestamp DESC LIMIT $limit";
            Result result = session.run(query, parameters("limit", limit));
            List<Metric> metrics = new ArrayList<>();
            while (result.hasNext()) {
                metrics.add(mapToMetric(result.next().get("m").asMap()));
            }
            return metrics;
        }
    }

    public void deleteOlderThan(LocalDateTime date) {
        try (Session session = driver.session()) {
            String query = "MATCH (m:Metric) WHERE m.timestamp < $date DETACH DELETE m";
            session.run(query, parameters("date", date.toString()));
        }
    }

    private Metric mapToMetric(Map<String, Object> map) {
        Metric metric = new Metric();
        metric.setId((String) map.get("id"));
        metric.setName((String) map.get("name"));
        metric.setDescription((String) map.get("description"));
        metric.setMetricType((String) map.get("metricType"));
        metric.setValue(map.get("value") != null ? (Double) map.get("value") : 0.0);
        metric.setUnit((String) map.get("unit"));
        metric.setEntityId((String) map.get("entityId"));
        metric.setEntityType((String) map.get("entityType"));
        metric.setPeriod((String) map.get("period"));
        if (map.get("timestamp") != null) {
            metric.setTimestamp(LocalDateTime.parse((String) map.get("timestamp")));
        }
        return metric;
    }
}
