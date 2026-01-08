package org.example.repository;

import org.example.model.AuditLog;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;


import java.time.LocalDateTime;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

/**
 * Audit Repository - Audit log data access
 */
public class AuditRepository {

    private final Driver driver;

    public AuditRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    /**
     * Save audit log entry
     */
    public void save(AuditLog log) {
        try (Session session = driver.session()) {
            String query = "CREATE (a:AuditLog {" +
                    "id: $id, action: $action, userId: $userId, username: $username, " +
                    "entityType: $entityType, entityId: $entityId, " +
                    "oldValue: $oldValue, newValue: $newValue, result: $result, " +
                    "ipAddress: $ipAddress, userAgent: $userAgent, timestamp: $timestamp" +
                    "})";

            session.run(query, parameters(
                    "id", log.getId(),
                    "action", log.getAction(),
                    "userId", log.getUserId(),
                    "username", log.getUsername(),
                    "entityType", log.getEntityType(),
                    "entityId", log.getEntityId(),
                    "oldValue", log.getOldValue(),
                    "newValue", log.getNewValue(),
                    "result", log.getResult(),
                    "ipAddress", log.getIpAddress(),
                    "userAgent", log.getUserAgent(),
                    "timestamp", log.getTimestamp().toString()
            ));
        }
    }

    /**
     * Find recent audit logs
     */
    public List<AuditLog> findRecent(int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog) " +
                    "RETURN a ORDER BY a.timestamp DESC LIMIT $limit";

            Result result = session.run(query, parameters("limit", limit));
            List<AuditLog> logs = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                logs.add(mapToAuditLog(record.get("a").asMap()));
            }

            return logs;
        } catch (Exception e) {
            System.err.println("Error finding audit logs: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Find logs by user
     */
    public List<AuditLog> findByUserId(String userId) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog {userId: $userId}) " +
                    "RETURN a ORDER BY a.timestamp DESC";

            Result result = session.run(query, parameters("userId", userId));
            List<AuditLog> logs = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                logs.add(mapToAuditLog(record.get("a").asMap()));
            }

            return logs;
        }
    }

    /**
     * Find logs by action
     */
    public List<AuditLog> findByAction(String action) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog {action: $action}) " +
                    "RETURN a ORDER BY a.timestamp DESC";

            Result result = session.run(query, parameters("action", action));
            List<AuditLog> logs = new ArrayList<>();

            while (result.hasNext()) {
                Record record = result.next();
                logs.add(mapToAuditLog(record.get("a").asMap()));
            }

            return logs;
        }
    }

    /**
     * Delete logs older than specified date
     */
    public void deleteOlderThan(LocalDateTime cutoffDate) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog) " +
                    "WHERE a.timestamp < $cutoff " +
                    "DELETE a";

            session.run(query, parameters("cutoff", cutoffDate.toString()));
        }
    }

    /**
     * Count logs by action
     */
    public Map<String, Long> countByAction() {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog) " +
                    "RETURN a.action as action, count(a) as count";

            Result result = session.run(query);
            Map<String, Long> counts = new HashMap<>();

            while (result.hasNext()) {
                Record record = result.next();
                counts.put(
                        record.get("action").asString(),
                        record.get("count").asLong()
                );
            }

            return counts;
        }
    }

    private AuditLog mapToAuditLog(Map<String, Object> map) {
        AuditLog log = new AuditLog();
        log.setId((String) map.get("id"));
        log.setAction((String) map.get("action"));
        log.setUserId((String) map.get("userId"));
        log.setUsername((String) map.get("username"));
        log.setEntityType((String) map.get("entityType"));
        log.setEntityId((String) map.get("entityId"));
        log.setOldValue((String) map.get("oldValue"));
        log.setNewValue((String) map.get("newValue"));
        log.setResult((String) map.get("result"));
        log.setIpAddress((String) map.get("ipAddress"));
        log.setUserAgent((String) map.get("userAgent"));

        if (map.get("timestamp") != null) {
            log.setTimestamp(LocalDateTime.parse((String) map.get("timestamp")));
        }

        return log;
    }
}