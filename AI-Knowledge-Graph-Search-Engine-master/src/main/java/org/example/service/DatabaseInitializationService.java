package org.example.service;

import org.example.repository.Neo4jConnection;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import static org.neo4j.driver.Values.parameters;

/**
 * Service to initialize database relationships for existing data
 * Run this once to create all missing relationships
 */
public class DatabaseInitializationService {

    private final Driver driver;

    public DatabaseInitializationService() {
        this.driver = Neo4jConnection.getInstance().getDriver();
    }

    /**
     * Initialize all relationships in the database
     */
    public void initializeAllRelationships() {
        System.out.println("🚀 Starting database relationship initialization...\n");

        createTicketToCategoryRelationships();
        createTicketToCreatorRelationships();
        createTicketToAssigneeRelationships();
        createUserToTeamRelationships();
        createTicketToSLARelationships();
        createCommentRelationships();
        createKBRelationships();
        createSimilarTicketRelationships();
        createTeamLeaderRelationships();

        System.out.println("\n✅ Database initialization complete!");
        printRelationshipStats();
    }

    /**
     * 1. Link Tickets to Categories
     */
    private void createTicketToCategoryRelationships() {
        System.out.println("1️⃣  Creating Ticket → Category relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (t:Ticket), (c:Category)
                    WHERE t.category = c.name OR t.category = c.id
                    MERGE (t)-[r:BELONGS_TO]->(c)
                    SET r.linkedAt = datetime()
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " Ticket→Category relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 2. Link Tickets to Creators
     */
    private void createTicketToCreatorRelationships() {
        System.out.println("2️⃣  Creating User → Ticket (CREATED) relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (t:Ticket), (u:User)
                    WHERE t.createdBy = u.username OR t.createdBy = u.id
                    MERGE (u)-[r:CREATED]->(t)
                    SET r.createdAt = t.createdAt
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " User→Ticket (CREATED) relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 3. Link Tickets to Assigned Users
     */
    private void createTicketToAssigneeRelationships() {
        System.out.println("3️⃣  Creating Ticket → User (ASSIGNED_TO) relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (t:Ticket), (u:User)
                    WHERE t.assignedTo IS NOT NULL 
                    AND (t.assignedTo = u.username OR t.assignedTo = u.id)
                    MERGE (t)-[r:ASSIGNED_TO]->(u)
                    SET r.assignedAt = datetime()
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " Ticket→User (ASSIGNED_TO) relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 4. Link Users to Teams
     */
    private void createUserToTeamRelationships() {
        System.out.println("4️⃣  Creating User → Team (MEMBER_OF) relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (u:User), (t:Team)
                    WHERE u.teamId = t.id AND u.teamId IS NOT NULL
                    MERGE (u)-[r:MEMBER_OF]->(t)
                    SET r.joinedAt = datetime()
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " User→Team relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 5. Link Tickets to SLA Policies
     */
    private void createTicketToSLARelationships() {
        System.out.println("5️⃣  Creating Ticket → SLA relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (t:Ticket), (s:SLA)
                    WHERE t.priority = s.priority AND s.active = true
                    MERGE (t)-[r:HAS_SLA]->(s)
                    SET r.appliedAt = datetime()
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " Ticket→SLA relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 6. Link Comments to Tickets and Users
     */
    private void createCommentRelationships() {
        System.out.println("6️⃣  Creating Comment relationships...");
        try (Session session = driver.session()) {
            // Comment → Ticket
            String query1 = """
                    MATCH (c:Comment), (t:Ticket)
                    WHERE c.ticketId = t.id
                    MERGE (c)-[r:ON_TICKET]->(t)
                    SET r.commentedAt = c.createdAt
                    RETURN count(r) as count
                    """;
            Result result1 = session.run(query1);
            if (result1.hasNext()) {
                long count = result1.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " Comment→Ticket relationships");
            }

            // User → Comment
            String query2 = """
                    MATCH (c:Comment), (u:User)
                    WHERE c.authorId = u.id OR c.authorId = u.username
                    MERGE (u)-[r:WROTE]->(c)
                    SET r.writtenAt = c.createdAt
                    RETURN count(r) as count
                    """;
            Result result2 = session.run(query2);
            if (result2.hasNext()) {
                long count = result2.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " User→Comment relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 7. Link Knowledge Base to Categories
     */
    private void createKBRelationships() {
        System.out.println("7️⃣  Creating KnowledgeBase → Category relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (kb:KnowledgeBase), (c:Category)
                    WHERE kb.category = c.name OR kb.category = c.id
                    MERGE (kb)-[r:RELATED_TO]->(c)
                    SET r.linkedAt = datetime()
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " KB→Category relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 8. Create Similar Ticket relationships
     */
    private void createSimilarTicketRelationships() {
        System.out.println("8️⃣  Creating SIMILAR_TO relationships between tickets...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (t1:Ticket)-[:BELONGS_TO]->(c:Category)<-[:BELONGS_TO]-(t2:Ticket)
                    WHERE t1.id < t2.id
                    MERGE (t1)-[r:SIMILAR_TO]->(t2)
                    SET r.reason = 'Same category: ' + c.name
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " SIMILAR_TO relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * 9. Link Team Leaders to Teams
     */
    private void createTeamLeaderRelationships() {
        System.out.println("9️⃣  Creating User → Team (LEADS) relationships...");
        try (Session session = driver.session()) {
            String query = """
                    MATCH (u:User), (t:Team)
                    WHERE u.id = t.leadId AND t.leadId IS NOT NULL
                    MERGE (u)-[r:LEADS]->(t)
                    SET r.leadSince = datetime()
                    RETURN count(r) as count
                    """;
            Result result = session.run(query);
            if (result.hasNext()) {
                long count = result.next().get("count").asLong();
                System.out.println("   ✓ Created " + count + " User→Team (LEADS) relationships\n");
            }
        } catch (Exception e) {
            System.err.println("   ❌ Error: " + e.getMessage() + "\n");
        }
    }

    /**
     * Print relationship statistics
     */
    private void printRelationshipStats() {
        System.out.println("\n📊 Relationship Statistics:");
        System.out.println("=" .repeat(50));

        try (Session session = driver.session()) {
            String query = """
                    MATCH ()-[r]->()
                    RETURN type(r) as RelationshipType, count(r) as Count
                    ORDER BY Count DESC
                    """;
            Result result = session.run(query);

            while (result.hasNext()) {
                Record record = result.next();
                String type = record.get("RelationshipType").asString();
                long count = record.get("Count").asLong();
                System.out.printf("   %-20s: %d%n", type, count);
            }

            System.out.println("=" .repeat(50));
        } catch (Exception e) {
            System.err.println("❌ Error getting stats: " + e.getMessage());
        }
    }

    /**
     * Delete all relationships (use with caution!)
     */
    public void deleteAllRelationships() {
        System.out.println("⚠️  Deleting all relationships...");
        try (Session session = driver.session()) {
            String query = "MATCH ()-[r]->() DELETE r";
            session.run(query);
            System.out.println("✅ All relationships deleted");
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
        }
    }

    /**
     * Verify database connection and structure
     */
    public void verifyDatabase() {
        System.out.println("\n🔍 Verifying Database Structure...\n");

        try (Session session = driver.session()) {
            // Check node counts
            String nodeQuery = """
                    MATCH (n)
                    RETURN labels(n)[0] as Label, count(n) as Count
                    ORDER BY Count DESC
                    """;
            System.out.println("📦 Node Counts:");
            Result nodeResult = session.run(nodeQuery);
            while (nodeResult.hasNext()) {
                Record record = nodeResult.next();
                System.out.printf("   %-20s: %d%n",
                        record.get("Label").asString("Unknown"),
                        record.get("Count").asLong());
            }

            // Check relationship counts
            System.out.println("\n🔗 Relationship Counts:");
            String relQuery = """
                    MATCH ()-[r]->()
                    RETURN type(r) as Type, count(r) as Count
                    ORDER BY Count DESC
                    """;
            Result relResult = session.run(relQuery);
            while (relResult.hasNext()) {
                Record record = relResult.next();
                System.out.printf("   %-20s: %d%n",
                        record.get("Type").asString(),
                        record.get("Count").asLong());
            }

        } catch (Exception e) {
            System.err.println("❌ Error verifying database: " + e.getMessage());
        }
    }

    /**
     * Check if initialization is needed
     * Returns true if there are nodes but few/no relationships
     */
    public boolean checkIfInitializationNeeded() {
        try (Session session = driver.session()) {
            // Count nodes
            String nodeQuery = "MATCH (n) RETURN count(n) as nodeCount";
            Result nodeResult = session.run(nodeQuery);
            long nodeCount = 0;
            if (nodeResult.hasNext()) {
                nodeCount = nodeResult.next().get("nodeCount").asLong();
            }

            // Count relationships
            String relQuery = "MATCH ()-[r]->() RETURN count(r) as relCount";
            Result relResult = session.run(relQuery);
            long relCount = 0;
            if (relResult.hasNext()) {
                relCount = relResult.next().get("relCount").asLong();
            }

            System.out.println("📊 Database check: " + nodeCount + " nodes, " + relCount + " relationships");

            // If we have nodes but very few relationships, we need initialization
            if (nodeCount > 0 && relCount < nodeCount) {
                System.out.println("⚠️  Low relationship count - initialization needed");
                return true;
            }

            // If we have no nodes, no initialization needed (empty database)
            if (nodeCount == 0) {
                System.out.println("📭 Empty database - no initialization needed");
                return false;
            }

            System.out.println("✅ Database appears to be initialized");
            return false;

        } catch (Exception e) {
            System.err.println("❌ Error checking database: " + e.getMessage());
            // If we can't check, assume initialization is needed
            return true;
        }
    }

    /**
     * Quick initialization - only creates missing relationships
     */
    public void quickInitialize() {
        System.out.println("⚡ Running quick initialization...\n");
        createTicketToCategoryRelationships();
        createTicketToCreatorRelationships();
        createTicketToAssigneeRelationships();
        System.out.println("\n✅ Quick initialization complete!");
    }

    /**
     * Main method to run initialization
     */
    public static void main(String[] args) {
        DatabaseInitializationService service = new DatabaseInitializationService();

        // Verify current state
        service.verifyDatabase();

        // Initialize relationships
        service.initializeAllRelationships();

        // Verify after initialization
        service.verifyDatabase();

        System.out.println("\n✨ Initialization complete! Check Neo4j Browser to see relationships.");
    }
}