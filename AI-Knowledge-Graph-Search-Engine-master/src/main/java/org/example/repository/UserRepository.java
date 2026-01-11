package org.example.repository;

import org.example.model.User;
import org.example.model.enums.UserRole;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.time.LocalDateTime;
import java.util.*;

import static org.neo4j.driver.Values.parameters;

public class UserRepository {
    private final Driver driver;

    public UserRepository() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    /**
     * Save user with relationships
     */
    public User save(User user) {
        try (Session session = driver.session()) {
            String query = "MERGE (u:User {id: $id}) " +
                    "SET u.username = $username, u.email = $email, u.password = $password, " +
                    "u.fullName = $fullName, u.role = $role, u.teamId = $teamId, " +
                    "u.department = $department, u.phone = $phone, u.active = $active, " +
                    "u.createdAt = $createdAt, u.avatarUrl = $avatarUrl " +
                    "RETURN u";

            session.run(query, parameters(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "password", user.getPassword(),
                    "fullName", user.getFullName(),
                    "role", user.getRole() != null ? user.getRole().name() : null,
                    "teamId", user.getTeamId(),
                    "department", user.getDepartment(),
                    "phone", user.getPhone(),
                    "active", user.isActive(),
                    "createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : LocalDateTime.now().toString(),
                    "avatarUrl", user.getAvatarUrl()));

            // Create team relationship if teamId exists
            if (user.getTeamId() != null && !user.getTeamId().isEmpty()) {
                createTeamRelationship(user.getId(), user.getTeamId());
            }

            System.out.println("✅ User saved: " + user.getUsername());
            return user;
        }
    }

    /**
     * Create MEMBER_OF relationship between User and Team
     */
    private void createTeamRelationship(String userId, String teamId) {
        try (Session session = driver.session()) {
            String query = """
                    MATCH (u:User {id: $userId})
                    MATCH (t:Team {id: $teamId})
                    MERGE (u)-[r:MEMBER_OF]->(t)
                    SET r.joinedAt = datetime()
                    RETURN r
                    """;
            session.run(query, parameters("userId", userId, "teamId", teamId));
            System.out.println("  ✓ User linked to team");
        } catch (Exception e) {
            System.err.println("  ❌ Error linking user to team: " + e.getMessage());
        }
    }

    /**
     * Find user by ID
     */
    public User findById(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {id: $id}) RETURN u";
            Result result = session.run(query, parameters("id", id));

            if (result.hasNext()) {
                Record record = result.next();
                return mapToUser(record.get("u").asMap());
            }
            return null;
        }
    }

    /**
     * Find user by username
     */
    public User findByUsername(String username) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {username: $username}) RETURN u";
            Result result = session.run(query, parameters("username", username));

            if (result.hasNext()) {
                Record record = result.next();
                return mapToUser(record.get("u").asMap());
            }
            return null;
        }
    }

    /**
     * Find all users
     */
    public List<User> findAll() {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User) RETURN u ORDER BY u.createdAt DESC";
            Result result = session.run(query);

            List<User> users = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                users.add(mapToUser(record.get("u").asMap()));
            }
            return users;
        }
    }

    /**
     * Find users by role
     */
    public List<User> findByRole(UserRole role) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {role: $role}) RETURN u";
            Result result = session.run(query, parameters("role", role.name()));

            List<User> users = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                users.add(mapToUser(record.get("u").asMap()));
            }
            return users;
        }
    }

    /**
     * Delete user
     */
    public void delete(String id) {
        try (Session session = driver.session()) {
            String query = "MATCH (u:User {id: $id}) DETACH DELETE u";
            session.run(query, parameters("id", id));
            System.out.println("✅ User deleted: " + id);
        }
    }

    /**
     * Map database record to User object
     */
    private User mapToUser(Map<String, Object> map) {
        User user = new User();
        user.setId((String) map.get("id"));
        user.setUsername((String) map.get("username"));
        user.setEmail((String) map.get("email"));
        user.setPassword((String) map.get("password"));
        user.setFullName((String) map.get("fullName"));
        if (map.get("role") != null) {
            user.setRole(UserRole.valueOf((String) map.get("role")));
        }
        user.setTeamId((String) map.get("teamId"));
        user.setDepartment((String) map.get("department"));
        user.setPhone((String) map.get("phone"));
        user.setActive(map.get("active") != null ? (Boolean) map.get("active") : true);
        user.setAvatarUrl((String) map.get("avatarUrl"));
        if (map.get("createdAt") != null) {
            user.setCreatedAt(LocalDateTime.parse((String) map.get("createdAt")));
        }
        return user;
    }
}