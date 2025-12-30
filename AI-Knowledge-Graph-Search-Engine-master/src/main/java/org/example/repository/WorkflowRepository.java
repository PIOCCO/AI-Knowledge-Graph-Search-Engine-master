package org.example.repository;

import org.example.model.Workflow;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class WorkflowRepository {
    private final Driver driver;

    public WorkflowRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public Workflow save(Workflow workflow) {
        try (Session session = driver.session()) {
            String query = "MERGE (w:Workflow {id: $id}) " +
                    "SET w.name = $name, w.description = $description, w.triggerEvent = $triggerEvent, " +
                    "w.active = $active, w.createdAt = $createdAt, w.createdBy = $createdBy, " +
                    "w.executionCount = $executionCount RETURN w";

            session.run(query, parameters(
                    "id", workflow.getId(),
                    "name", workflow.getName(),
                    "description", workflow.getDescription(),
                    "triggerEvent", workflow.getTriggerEvent(),
                    "active", workflow.isActive(),
                    "createdAt",
                    workflow.getCreatedAt() != null ? workflow.getCreatedAt().toString()
                            : LocalDateTime.now().toString(),
                    "createdBy", workflow.getCreatedBy(),
                    "executionCount", workflow.getExecutionCount()));
            return workflow;
        }
    }

    public Workflow findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (w:Workflow {id: $id}) RETURN w";
            Result result = session.run(query, parameters("id", id));
            if (result.hasNext()) {
                return mapToWorkflow(result.next().get("w").asMap());
            }
            return null;
        }
    }

    public List<Workflow> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (w:Workflow) RETURN w ORDER BY w.name";
            Result result = session.run(query);
            List<Workflow> workflows = new ArrayList<>();
            while (result.hasNext()) {
                workflows.add(mapToWorkflow(result.next().get("w").asMap()));
            }
            return workflows;
        }
    }

    public List<Workflow> findActiveWorkflows() {
        try (Session session = driver.session()) {
            String query = "MATCH (w:Workflow {active: true}) RETURN w ORDER BY w.name";
            Result result = session.run(query);
            List<Workflow> workflows = new ArrayList<>();
            while (result.hasNext()) {
                workflows.add(mapToWorkflow(result.next().get("w").asMap()));
            }
            return workflows;
        }
    }

    public List<Workflow> findByTriggerEvent(String triggerEvent) {
        try (Session session = driver.session()) {
            String query = "MATCH (w:Workflow {triggerEvent: $triggerEvent, active: true}) RETURN w";
            Result result = session.run(query, parameters("triggerEvent", triggerEvent));
            List<Workflow> workflows = new ArrayList<>();
            while (result.hasNext()) {
                workflows.add(mapToWorkflow(result.next().get("w").asMap()));
            }
            return workflows;
        }
    }

    public void delete(String id) {
        try (Session session = driver.session()) {
            session.run("MATCH (w:Workflow {id: $id}) DETACH DELETE w", parameters("id", id));
        }
    }

    private Workflow mapToWorkflow(Map<String, Object> map) {
        Workflow workflow = new Workflow();
        workflow.setId((String) map.get("id"));
        workflow.setName((String) map.get("name"));
        workflow.setDescription((String) map.get("description"));
        workflow.setTriggerEvent((String) map.get("triggerEvent"));
        workflow.setActive(map.get("active") != null ? (Boolean) map.get("active") : true);
        workflow.setCreatedBy((String) map.get("createdBy"));
        workflow.setExecutionCount(
                map.get("executionCount") != null ? ((Long) map.get("executionCount")).intValue() : 0);
        if (map.get("createdAt") != null) {
            workflow.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return workflow;
    }
}
