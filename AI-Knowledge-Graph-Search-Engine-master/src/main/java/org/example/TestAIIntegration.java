package org.example;

import org.example.model.Ticket;
import org.example.model.ClassificationResult;
import org.example.service.MLClassificationService;

import java.util.concurrent.CompletableFuture;

/**
 * proper Standalone test to verify AI Integration
 */
public class TestAIIntegration {

    public static void main(String[] args) {
        System.out.println("🚀 Starting AI Integration Test...");

        // 1. Initialize Service
        MLClassificationService mlService = new MLClassificationService();

        // 2. Create a test ticket
        Ticket testTicket = new Ticket();
        testTicket.setId("TEST-001");
        testTicket.setTitle("Database connection failed");
        testTicket.setDescription(
                "I cannot connect to the production database. It keeps timing out after 30 seconds. I suspect a firewall issue.");
        testTicket.setPriority("High");

        System.out.println("\n📝 Test Ticket:");
        System.out.println("   Title: " + testTicket.getTitle());
        System.out.println("   Description: " + testTicket.getDescription());

        // 3. Call AI Service
        System.out.println("\n⏳ Sending to Python AI Agent...");
        try {
            CompletableFuture<ClassificationResult> future = mlService.classifyTicketAsync(testTicket);
            ClassificationResult result = future.join(); // Wait for result

            if (result != null) {
                System.out.println("\n✅ SUCCESS! AI Response Received:");
                System.out.println("   Predicted Category: " + result.getCategoryName());
                System.out.println("   Confidence: " + String.format("%.2f%%", result.getConfidence() * 100));
                System.out.println("   Raw ID: " + result.getPredictedCategory());
            } else {
                System.err.println("\n❌ FAILED: Received null response. Is the Python service running?");
                System.err.println(
                        "   Make sure to run: 'python ml_service.py' in the Ticket_Agent/ml_service directory.");
            }
        } catch (Exception e) {
            System.err.println("\n❌ EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n🏁 Test Completed.");
        System.exit(0);
    }
}
