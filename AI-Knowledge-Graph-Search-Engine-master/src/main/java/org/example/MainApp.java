package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import org.example.config.AppConfig;
import org.example.repository.Neo4jConnection;
import org.example.service.DatabaseInitializationService;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("🚀 AI Knowledge Graph Search Engine - GUI Starting...");

        AppConfig config = AppConfig.getInstance();

        // Test Neo4j Connection
        if (!Neo4jConnection.getInstance().testConnection()) {
            System.err.println("❌ Neo4j connection failed. Please start Neo4j database.");

            String neo4jPassword = config.getProperty("neo4j.password", "N/A");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot connect to Neo4j");
            alert.setContentText(
                    "Please ensure Neo4j is running at bolt://localhost:7687\n" +
                            "Username: neo4j\n" +
                            "Password: " + neo4jPassword);
            alert.showAndWait();
            System.exit(1);
        }

        // Initialize database relationships (runs once on startup)
        initializeDatabaseRelationships(primaryStage);

        // Load main window
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle(config.getAppName());
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("✅ Application started successfully!");
    }

    /**
     * Initialize database relationships on startup
     */
    private void initializeDatabaseRelationships(Stage primaryStage) {
        // Create loading dialog
        Alert loadingDialog = new Alert(Alert.AlertType.INFORMATION);
        loadingDialog.setTitle("Initializing Database");
        loadingDialog.setHeaderText("Setting up database relationships...");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);
        javafx.scene.control.Label statusLabel = new javafx.scene.control.Label("Please wait...");
        content.getChildren().addAll(progressIndicator, statusLabel);

        loadingDialog.getDialogPane().setContent(content);
        loadingDialog.getDialogPane().getButtonTypes().clear();
        loadingDialog.show();

        // Run initialization in background thread
        Thread initThread = new Thread(() -> {
            try {
                System.out.println("\n🔧 Initializing database relationships...");

                DatabaseInitializationService service = new DatabaseInitializationService();

                // Check if relationships already exist
                updateStatus(statusLabel, "Checking database...");
                boolean needsInit = service.checkIfInitializationNeeded();

                if (needsInit) {
                    updateStatus(statusLabel, "Creating relationships...");
                    service.initializeAllRelationships();
                    System.out.println("✅ Database initialization complete!");

                    // Show success message on UI thread
                    javafx.application.Platform.runLater(() -> {
                        loadingDialog.close();
                        showInitializationSuccess();
                    });
                } else {
                    System.out.println("✅ Database already initialized - skipping");
                    javafx.application.Platform.runLater(() -> {
                        loadingDialog.close();
                    });
                }

            } catch (Exception e) {
                System.err.println("❌ Error during database initialization: " + e.getMessage());
                e.printStackTrace();

                // Show error on UI thread
                javafx.application.Platform.runLater(() -> {
                    loadingDialog.close();
                    showInitializationError(e);
                });
            }
        });

        initThread.setDaemon(true);
        initThread.start();
    }

    /**
     * Update status label from background thread
     */
    private void updateStatus(javafx.scene.control.Label label, String text) {
        javafx.application.Platform.runLater(() -> label.setText(text));
    }

    /**
     * Show success message
     */
    private void showInitializationSuccess() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Database Initialized");
        alert.setHeaderText("✅ Database Setup Complete");
        alert.setContentText(
                "All relationships have been created successfully!\n\n" +
                        "You can now:\n" +
                        "• Create and manage tickets\n" +
                        "• View relationship graphs in Neo4j Browser\n" +
                        "• Use all features of the system"
        );

        // Auto-close after 3 seconds
        Thread autoClose = new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            } catch (InterruptedException e) {
                // Ignore
            }
        });
        autoClose.setDaemon(true);
        autoClose.start();

        alert.show();
    }

    /**
     * Show error message
     */
    private void showInitializationError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Database Initialization");
        alert.setHeaderText("Database initialization had issues");
        alert.setContentText(
                "The application will still work, but some relationships may be missing.\n\n" +
                        "Error: " + e.getMessage() + "\n\n" +
                        "You can manually initialize later from Settings > Database Tools"
        );
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}