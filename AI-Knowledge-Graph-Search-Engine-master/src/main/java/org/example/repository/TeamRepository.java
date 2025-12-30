package org.example.repository;

import org.example.model.Team;
import org.neo4j.driver.*;
import java.time.LocalDateTime;
import java.util.*;
import static org.neo4j.driver.Values.parameters;

public class TeamRepository {
    private final Driver driver;

    public TeamRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    public Team save(Team team) {
        try (Session session = driver.session()) {
            String query = "MERGE (t:Team {id: $id}) " +
                    "SET t.name = $name, t.description = $description, t.leadId = $leadId, " +
                    "t.department = $department, t.active = $active, t.createdAt = $createdAt, " +
                    "t.maxCapacity = $maxCapacity RETURN t";

            session.run(query, parameters(
                    "id", team.getId(),
                    "name", team.getName(),
                    "description", team.getDescription(),
                    "leadId", team.getLeadId(),
                    "department", team.getDepartment(),
                    "active", team.isActive(),
                    "createdAt",
                    team.getCreatedAt() != null ? team.getCreatedAt().toString() : LocalDateTime.now().toString(),
                    "maxCapacity", team.getMaxCapacity()));
            return team;
        }
    }

    public Team findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (t:Team {id: $id}) RETURN t";
            Result result = session.run(query, parameters("id", id));
            if (result.hasNext()) {
                return mapToTeam(result.next().get("t").asMap());
            }
            return null;
        }
    }

    public List<Team> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (t:Team) RETURN t ORDER BY t.name";
            Result result = session.run(query);
            List<Team> teams = new ArrayList<>();
            while (result.hasNext()) {
                teams.add(mapToTeam(result.next().get("t").asMap()));
            }
            return teams;
        }
    }

    public void delete(String id) {
        try (Session session = driver.session()) {
            session.run("MATCH (t:Team {id: $id}) DETACH DELETE t", parameters("id", id));
        }
    }

    private Team mapToTeam(Map<String, Object> map) {
        Team team = new Team();
        team.setId((String) map.get("id"));
        team.setName((String) map.get("name"));
        team.setDescription((String) map.get("description"));
        team.setLeadId((String) map.get("leadId"));
        team.setDepartment((String) map.get("department"));
        team.setActive(map.get("active") != null ? (Boolean) map.get("active") : true);
        team.setMaxCapacity(map.get("maxCapacity") != null ? ((Long) map.get("maxCapacity")).intValue() : 10);
        if (map.get("createdAt") != null) {
            team.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return team;
    }
}
