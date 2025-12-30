package org.example.util;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.example.repository.Neo4jConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphUtils {

    private static final Driver driver = Neo4jConnection.getInstance().getDriver();

    public static List<Map<String, Object>> executeQuery(String query, Map<String, Object> parameters) {
        try (Session session = driver.session()) {
            Result result = session.run(query, parameters);
            List<Map<String, Object>> results = new ArrayList<>();
            while (result.hasNext()) {
                results.add(result.next().asMap());
            }
            return results;
        }
    }

    public static Map<String, Object> executeSingleQuery(String query, Map<String, Object> parameters) {
        try (Session session = driver.session()) {
            Result result = session.run(query, parameters);
            if (result.hasNext()) {
                return result.next().asMap();
            }
            return new HashMap<>();
        }
    }

    public static void executeUpdate(String query, Map<String, Object> parameters) {
        try (Session session = driver.session()) {
            session.run(query, parameters);
        }
    }

    public static long countNodes(String label) {
        String query = "MATCH (n:" + label + ") RETURN count(n) as count";
        Map<String, Object> result = executeSingleQuery(query, new HashMap<>());
        return result.containsKey("count") ? (Long) result.get("count") : 0;
    }

    public static long countRelationships(String type) {
        String query = "MATCH ()-[r:" + type + "]->() RETURN count(r) as count";
        Map<String, Object> result = executeSingleQuery(query, new HashMap<>());
        return result.containsKey("count") ? (Long) result.get("count") : 0;
    }

    public static void createRelationship(String fromId, String fromLabel, String toId, String toLabel,
            String relationshipType) {
        String query = "MATCH (a:" + fromLabel + " {id: $fromId}), (b:" + toLabel + " {id: $toId}) " +
                "MERGE (a)-[r:" + relationshipType + "]->(b) RETURN r";
        Map<String, Object> params = new HashMap<>();
        params.put("fromId", fromId);
        params.put("toId", toId);
        executeUpdate(query, params);
    }

    public static void deleteRelationship(String fromId, String toId, String relationshipType) {
        String query = "MATCH (a {id: $fromId})-[r:" + relationshipType + "]->(b {id: $toId}) DELETE r";
        Map<String, Object> params = new HashMap<>();
        params.put("fromId", fromId);
        params.put("toId", toId);
        executeUpdate(query, params);
    }

    public static List<Map<String, Object>> findRelatedNodes(String nodeId, String relationshipType, String direction) {
        String query;
        if ("OUTGOING".equals(direction)) {
            query = "MATCH (n {id: $nodeId})-[:" + relationshipType + "]->(m) RETURN m";
        } else if ("INCOMING".equals(direction)) {
            query = "MATCH (n {id: $nodeId})<-[:" + relationshipType + "]-(m) RETURN m";
        } else {
            query = "MATCH (n {id: $nodeId})-[:" + relationshipType + "]-(m) RETURN m";
        }

        Map<String, Object> params = new HashMap<>();
        params.put("nodeId", nodeId);
        return executeQuery(query, params);
    }

    public static void clearDatabase() {
        executeUpdate("MATCH (n) DETACH DELETE n", new HashMap<>());
    }

    public static Map<String, Long> getDatabaseStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalNodes", countAllNodes());
        stats.put("totalRelationships", countAllRelationships());
        return stats;
    }

    private static long countAllNodes() {
        String query = "MATCH (n) RETURN count(n) as count";
        Map<String, Object> result = executeSingleQuery(query, new HashMap<>());
        return result.containsKey("count") ? (Long) result.get("count") : 0;
    }

    private static long countAllRelationships() {
        String query = "MATCH ()-[r]->() RETURN count(r) as count";
        Map<String, Object> result = executeSingleQuery(query, new HashMap<>());
        return result.containsKey("count") ? (Long) result.get("count") : 0;
    }
}
