package org.example;

import org.example.service.DatabaseInitializationService;

/**
 * Standalone application to initialize database relationships
 * Run this AFTER you have data in your Neo4j database
 *
 * To run from command line:
 * java -cp target/classes org.example.InitializeDatabaseApp
 */
public class InitializeDatabaseApp {

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║   Neo4j Relationship Initialization Tool              ║");
        System.out.println("║   AI Ticket Management System                          ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        try {
            DatabaseInitializationService service = new DatabaseInitializationService();

            // Step 1: Verify current state
            System.out.println("STEP 1: Verifying current database state...");
            System.out.println("─".repeat(60));
            service.verifyDatabase();

            // Step 2: Ask for confirmation
            System.out.println("\n\nSTEP 2: Ready to initialize relationships");
            System.out.println("─".repeat(60));
            System.out.print("Do you want to continue? (yes/no): ");

            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();

            if (!response.equals("yes") && !response.equals("y")) {
                System.out.println("\n❌ Operation cancelled by user.");
                return;
            }

            // Step 3: Initialize relationships
            System.out.println("\n\nSTEP 3: Initializing relationships...");
            System.out.println("─".repeat(60));
            service.initializeAllRelationships();

            // Step 4: Verify results
            System.out.println("\n\nSTEP 4: Verifying results...");
            System.out.println("─".repeat(60));
            service.verifyDatabase();

            // Success message
            System.out.println("\n\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║   ✅ INITIALIZATION COMPLETE!                          ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("\n📌 Next steps:");
            System.out.println("   1. Open Neo4j Browser (http://localhost:7474)");
            System.out.println("   2. Run: MATCH (n)-[r]->(m) RETURN n, r, m LIMIT 100");
            System.out.println("   3. You should see all relationships visualized!\n");

        } catch (Exception e) {
            System.err.println("\n\n╔════════════════════════════════════════════════════════╗");
            System.err.println("║   ❌ ERROR DURING INITIALIZATION                       ║");
            System.err.println("╚════════════════════════════════════════════════════════╝\n");
            System.err.println("Error details:");
            e.printStackTrace();
            System.err.println("\n💡 Troubleshooting tips:");
            System.err.println("   1. Make sure Neo4j is running");
            System.err.println("   2. Check your connection settings in application.properties");
            System.err.println("   3. Verify your Neo4j password is correct");
            System.err.println("   4. Make sure you have data in your database\n");
        }
    }
}