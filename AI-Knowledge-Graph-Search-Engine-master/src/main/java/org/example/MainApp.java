package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.example.repository.Neo4jConnection;

/**
 * JavaFX Main Application Entry Point
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("🚀 AI Knowledge Graph Search Engine - GUI Starting...");

        // Test Neo4j Connection
        if (!Neo4jConnection.getInstance().testConnection()) {
            System.err.println("❌ Neo4j connection failed. Please start Neo4j database.");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot connect to Neo4j");
            alert.setContentText(
                    "Please ensure Neo4j is running at bolt://localhost:7687\nUsername: neo4j\nPassword: 11111111");
            alert.showAndWait();
            System.exit(1);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("AI Knowledge Graph Search Engine");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
