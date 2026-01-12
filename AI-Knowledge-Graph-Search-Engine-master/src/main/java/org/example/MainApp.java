package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.config.AppConfig;
import org.example.repository.Neo4jConnection;

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

    public static void main(String[] args) {
        launch(args);
    }
}