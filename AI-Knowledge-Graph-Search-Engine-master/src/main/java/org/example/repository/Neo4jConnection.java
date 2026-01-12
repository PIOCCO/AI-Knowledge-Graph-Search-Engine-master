package org.example.repository;

import org.example.config.AppConfig;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

public class Neo4jConnection {
    private static Neo4jConnection instance;
    private final Driver driver;

    private Neo4jConnection() {
        // Load credentials from AppConfig
        AppConfig config = AppConfig.getInstance();

        String uri = config.getProperty("neo4j.uri", "bolt://127.0.0.1:7687");
        String username = config.getProperty("neo4j.username", "neo4j");
        String password = config.getProperty("neo4j.password", "00000000");

        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
        System.out.println("Connected to Neo4j at " + uri);
        initializeSchema();
    }

    private void initializeSchema() {
        try (Session session = getSession()) {
            // 1. Constraints (prevent duplicates)
            session.run("CREATE CONSTRAINT ticket_id IF NOT EXISTS FOR (t:Ticket) REQUIRE t.id IS UNIQUE");
            session.run("CREATE CONSTRAINT user_username IF NOT EXISTS FOR (u:User) REQUIRE u.username IS UNIQUE");
            session.run("CREATE CONSTRAINT category_name IF NOT EXISTS FOR (c:Category) REQUIRE c.name IS UNIQUE");

            // 2. Indexes for performance
            session.run("CREATE INDEX ticket_status IF NOT EXISTS FOR (t:Ticket) ON (t.status)");
            session.run("CREATE INDEX ticket_priority IF NOT EXISTS FOR (t:Ticket) ON (t.priority)");

            System.out.println("✅ Database schema initialized (constraints and indexes)");
        } catch (Exception e) {
            System.err.println("⚠️ Could not initialize schema: " + e.getMessage());
        }
    }

    public static Neo4jConnection getInstance() {
        if (instance == null) {
            instance = new Neo4jConnection();
        }
        return instance;
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        if (driver != null) {
            driver.close();
            System.out.println("Neo4j connection closed");
        }
    }

    public Driver getDriver() {
        return driver;
    }

    public boolean testConnection() {
        try (Session session = getSession()) {
            session.run("RETURN 1").consume();
            return true;
        } catch (Exception e) {
            System.err.println("Neo4j connection failed: " + e.getMessage());
            return false;
        }
    }
}
