package org.example.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.example.service.DatabaseInitializationService;

/**
 * Database Settings Dialog
 * Add this to your Settings menu or create a "Database Tools" button
 */
public class DatabaseSettingsDialog extends Dialog<Void> {

    private DatabaseInitializationService dbService;
    private TextArea logArea;

    public DatabaseSettingsDialog() {
        this.dbService = new DatabaseInitializationService();

        setTitle("Database Tools");
        setHeaderText("Database Relationship Management");

        // Create content
        VBox content = createContent();

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Set size
        getDialogPane().setPrefSize(700, 500);
    }

    private VBox createContent() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        // Status section
        Label statusLabel = new Label("Database Status");
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Button checkButton = new Button("🔍 Check Database Status");
        checkButton.setOnAction(e -> checkDatabaseStatus());
        checkButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 10px 20px;");

        // Actions section
        Label actionsLabel = new Label("Actions");
        actionsLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox buttonBox = new HBox(10);

        Button initButton = new Button("🔧 Initialize All Relationships");
        initButton.setOnAction(e -> initializeRelationships());
        initButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button quickFixButton = new Button("⚡ Quick Fix");
        quickFixButton.setOnAction(e -> quickFix());
        quickFixButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 10px 20px;");

        Button clearButton = new Button("🗑️ Clear All Relationships");
        clearButton.setOnAction(e -> clearRelationships());
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 10px 20px;");

        buttonBox.getChildren().addAll(initButton, quickFixButton, clearButton);

        // Log area
        Label logLabel = new Label("Activity Log");
        logLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefRowCount(15);
        logArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

        // Add all to vbox
        vbox.getChildren().addAll(
                statusLabel, checkButton,
                new Separator(),
                actionsLabel, buttonBox,
                new Separator(),
                logLabel, logArea
        );

        return vbox;
    }

    private void checkDatabaseStatus() {
        log("🔍 Checking database status...");

        new Thread(() -> {
            try {
                // Redirect System.out to capture logs
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(baos);
                java.io.PrintStream old = System.out;
                System.setOut(ps);

                dbService.verifyDatabase();
                boolean needsInit = dbService.checkIfInitializationNeeded();

                System.out.flush();
                System.setOut(old);
                String output = baos.toString();

                javafx.application.Platform.runLater(() -> {
                    log(output);
                    if (needsInit) {
                        log("\n⚠️  Database needs initialization!");
                    } else {
                        log("\n✅ Database is properly initialized");
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    log("❌ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void initializeRelationships() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Initialize Database");
        confirm.setHeaderText("Initialize all relationships?");
        confirm.setContentText(
                "This will create all missing relationships.\n" +
                        "Safe to run multiple times (uses MERGE).\n\n" +
                        "Continue?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log("\n🔧 Initializing database relationships...");

                new Thread(() -> {
                    try {
                        // Redirect System.out
                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        java.io.PrintStream ps = new java.io.PrintStream(baos);
                        java.io.PrintStream old = System.out;
                        System.setOut(ps);

                        dbService.initializeAllRelationships();

                        System.out.flush();
                        System.setOut(old);
                        String output = baos.toString();

                        javafx.application.Platform.runLater(() -> {
                            log(output);
                            log("\n✅ Initialization complete!");
                            showAlert("Success", "Database initialized successfully!", Alert.AlertType.INFORMATION);
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            log("❌ Error: " + e.getMessage());
                            showAlert("Error", "Initialization failed: " + e.getMessage(), Alert.AlertType.ERROR);
                        });
                    }
                }).start();
            }
        });
    }

    private void quickFix() {
        log("\n⚡ Running quick fix...");

        new Thread(() -> {
            try {
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                java.io.PrintStream ps = new java.io.PrintStream(baos);
                java.io.PrintStream old = System.out;
                System.setOut(ps);

                dbService.quickInitialize();

                System.out.flush();
                System.setOut(old);
                String output = baos.toString();

                javafx.application.Platform.runLater(() -> {
                    log(output);
                    showAlert("Success", "Quick fix complete!", Alert.AlertType.INFORMATION);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    log("❌ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    private void clearRelationships() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Clear Relationships");
        confirm.setHeaderText("⚠️ WARNING: Delete all relationships?");
        confirm.setContentText(
                "This will DELETE all relationships in the database!\n" +
                        "Nodes will remain, but all connections will be removed.\n\n" +
                        "This action cannot be undone!\n\n" +
                        "Are you sure?"
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                log("\n🗑️  Clearing all relationships...");

                new Thread(() -> {
                    try {
                        dbService.deleteAllRelationships();

                        javafx.application.Platform.runLater(() -> {
                            log("✅ All relationships deleted");
                            showAlert("Success", "All relationships have been deleted", Alert.AlertType.INFORMATION);
                        });

                    } catch (Exception e) {
                        javafx.application.Platform.runLater(() -> {
                            log("❌ Error: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
    }

    private void log(String message) {
        javafx.application.Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show the database settings dialog
     */
    public static void display() {
        DatabaseSettingsDialog dialog = new DatabaseSettingsDialog();
        dialog.showAndWait();
    }
}