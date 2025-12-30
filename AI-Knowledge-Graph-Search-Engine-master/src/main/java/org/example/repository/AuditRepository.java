package org.example.repository;

import org.example.model.AuditLog;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class AuditRepository {
    private final Driver driver;

    public AuditRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public AuditLog save(AuditLog auditLog) {
        try (Session session = driver.session()) {
            String query = "CREATE (a:AuditLog {id: $id, action: $action, userId: $userId, username: $username, " +
                    "entityType: $entityType, entityId: $entityId, oldValue: $oldValue, newValue: $newValue, " +
                    "timestamp: $timestamp, ipAddress: $ipAddress, userAgent: $userAgent, result: $result}) RETURN a";

            session.run(query, parameters(
                    "id", auditLog.getId(),
                    "action", auditLog.getAction(),
                    "userId", auditLog.getUserId(),
                    "username", auditLog.getUsername(),
                    "entityType", auditLog.getEntityType(),
                    "entityId", auditLog.getEntityId(),
                    "oldValue", auditLog.getOldValue(),
                    "newValue", auditLog.getNewValue(),
                    "timestamp",
                    auditLog.getTimestamp() != null ? auditLog.getTimestamp().toString()
                            : LocalDateTime.now().toString(),
                    "ipAddress", auditLog.getIpAddress(),
                    "userAgent", auditLog.getUserAgent(),
                    "result", auditLog.getResult()));
            return auditLog;
        }
    }

    public List<AuditLog> findByUserId(String userId, int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog {userId: $userId}) RETURN a ORDER BY a.timestamp DESC LIMIT $limit";
            Result result = session.run(query, parameters("userId", userId, "limit", limit));
            List<AuditLog> logs = new ArrayList<>();
            while (result.hasNext()) {
                logs.add(mapToAuditLog(result.next().get("a").asMap()));
            }
            return logs;
        }
    }

    public List<AuditLog> findByEntityId(String entityType, String entityId) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog {entityType: $entityType, entityId: $entityId}) RETURN a ORDER BY a.timestamp DESC";
            Result result = session.run(query, parameters("entityType", entityType, "entityId", entityId));
            List<AuditLog> logs = new ArrayList<>();
            while (result.hasNext()) {
                logs.add(mapToAuditLog(result.next().get("a").asMap()));
            }
            return logs;
        }
    }

    public List<AuditLog> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog) WHERE a.timestamp >= $startDate AND a.timestamp <= $endDate " +
                    "RETURN a ORDER BY a.timestamp DESC LIMIT $limit";
            Result result = session.run(query, parameters(
                    "startDate", startDate.toString(),
                    "endDate", endDate.toString(),
                    "limit", limit));
            List<AuditLog> logs = new ArrayList<>();
            while (result.hasNext()) {
                logs.add(mapToAuditLog(result.next().get("a").asMap()));
            }
            return logs;
        }
    }

    public List<AuditLog> findRecent(int limit) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog) RETURN a ORDER BY a.timestamp DESC LIMIT $limit";
            Result result = session.run(query, parameters("limit", limit));
            List<AuditLog> logs = new ArrayList<>();
            while (result.hasNext()) {
                logs.add(mapToAuditLog(result.next().get("a").asMap()));
            }
            return logs;
        }
    }

    public void deleteOlderThan(LocalDateTime date) {
        try (Session session = driver.session()) {
            String query = "MATCH (a:AuditLog) WHERE a.timestamp < $date DETACH DELETE a";
            session.run(query, parameters("date", date.toString()));
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
        log.setIpAddress((String) map.get("ipAddress"));
        log.setUserAgent((String) map.get("userAgent"));
        log.setResult((String) map.get("result"));
        if (map.get("timestamp") != null) {
            log.setTimestamp(LocalDateTime.parse((String) map.get("timestamp")));
        }
        return log;
    }
}
