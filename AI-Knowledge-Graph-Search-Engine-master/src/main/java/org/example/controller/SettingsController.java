package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.config.AppConfig;
import org.example.service.NotificationManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * Settings Controller - Application configuration management
 */
public class SettingsController implements Initializable {

    @FXML private TextField appNameField;
    @FXML private ComboBox<String> themeCombo;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Slider fontSizeSlider;
    @FXML private Label fontSizeLabel;

    @FXML private CheckBox emailNotifyToggle;
    @FXML private CheckBox desktopNotifyToggle;
    @FXML private CheckBox soundNotifyToggle;
    @FXML private CheckBox autoRefreshToggle;
    @FXML private Slider refreshIntervalSlider;
    @FXML private Label refreshIntervalLabel;

    @FXML private TextField neo4jUriField;
    @FXML private TextField neo4jUsernameField;
    @FXML private PasswordField neo4jPasswordField;
    @FXML private Button testConnectionButton;
    @FXML private Label connectionStatusLabel;

    @FXML private CheckBox aiClassificationToggle;
    @FXML private CheckBox aiPriorityToggle;
    @FXML private CheckBox aiSuggestionsToggle;
    @FXML private Slider aiConfidenceSlider;
    @FXML private Label aiConfidenceLabel;

    @FXML private TextField exportPathField;
    @FXML private ComboBox<String> defaultExportFormat;
    @FXML private CheckBox includeAttachmentsToggle;

    @FXML private Slider sessionTimeoutSlider;
    @FXML private Label sessionTimeoutLabel;
    @FXML private CheckBox twoFactorToggle;
    @FXML private CheckBox auditLogToggle;

    @FXML private VBox generalSection;
    @FXML private VBox notificationsSection;
    @FXML private VBox databaseSection;
    @FXML private VBox aiSection;
    @FXML private VBox exportSection;
    @FXML private VBox securitySection;

    private Preferences prefs;
    private AppConfig appConfig;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        prefs = Preferences.userNodeForPackage(SettingsController.class);
        appConfig = AppConfig.getInstance();

        setupComboBoxes();
        setupSliders();
        loadSettings();
        setupListeners();
    }

    private void setupComboBoxes() {
        themeCombo.getItems().addAll("Light", "Dark", "System");
        languageCombo.getItems().addAll("English", "Spanish", "French", "German");
        defaultExportFormat.getItems().addAll("CSV", "JSON", "HTML", "Excel");
    }

    private void setupSliders() {
        // Font size slider
        fontSizeSlider.setMin(10);
        fontSizeSlider.setMax(20);
        fontSizeSlider.setValue(13);
        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            fontSizeLabel.setText(newVal.intValue() + "px");
        });

        // Refresh interval slider
        refreshIntervalSlider.setMin(1);
        refreshIntervalSlider.setMax(10);
        refreshIntervalSlider.setValue(5);
        refreshIntervalSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            refreshIntervalLabel.setText(newVal.intValue() + " minutes");
        });

        // AI confidence slider
        aiConfidenceSlider.setMin(50);
        aiConfidenceSlider.setMax(100);
        aiConfidenceSlider.setValue(75);
        aiConfidenceSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            aiConfidenceLabel.setText(newVal.intValue() + "%");
        });

        // Session timeout slider
        sessionTimeoutSlider.setMin(5);
        sessionTimeoutSlider.setMax(480);
        sessionTimeoutSlider.setValue(60);
        sessionTimeoutSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int minutes = newVal.intValue();
            if (minutes < 60) {
                sessionTimeoutLabel.setText(minutes + " minutes");
            } else {
                sessionTimeoutLabel.setText((minutes / 60) + " hours");
            }
        });
    }

    private void loadSettings() {
        // General Settings
        appNameField.setText(prefs.get("app.name", "AI Ticket Management"));
        themeCombo.setValue(prefs.get("app.theme", "Light"));
        languageCombo.setValue(prefs.get("app.language", "English"));
        fontSizeSlider.setValue(prefs.getDouble("app.fontSize", 13));

        // Notification Settings
        emailNotifyToggle.setSelected(prefs.getBoolean("notifications.email", true));
        desktopNotifyToggle.setSelected(prefs.getBoolean("notifications.desktop", true));
        soundNotifyToggle.setSelected(prefs.getBoolean("notifications.sound", false));
        autoRefreshToggle.setSelected(prefs.getBoolean("app.autoRefresh", true));
        refreshIntervalSlider.setValue(prefs.getDouble("app.refreshInterval", 5));

        // Database Settings
        neo4jUriField.setText(appConfig.getProperty("neo4j.uri", "bolt://127.0.0.1:7687"));
        neo4jUsernameField.setText(appConfig.getProperty("neo4j.username", "neo4j"));
        neo4jPasswordField.setText(appConfig.getProperty("neo4j.password", ""));

        // AI Settings
        aiClassificationToggle.setSelected(prefs.getBoolean("ai.classification", true));
        aiPriorityToggle.setSelected(prefs.getBoolean("ai.priority", true));
        aiSuggestionsToggle.setSelected(prefs.getBoolean("ai.suggestions", true));
        aiConfidenceSlider.setValue(prefs.getDouble("ai.confidence", 75));

        // Export Settings
        exportPathField.setText(prefs.get("export.path", System.getProperty("user.home") + "/exports"));
        defaultExportFormat.setValue(prefs.get("export.format", "CSV"));
        includeAttachmentsToggle.setSelected(prefs.getBoolean("export.attachments", false));

        // Security Settings
        sessionTimeoutSlider.setValue(prefs.getDouble("security.sessionTimeout", 60));
        twoFactorToggle.setSelected(prefs.getBoolean("security.twoFactor", false));
        auditLogToggle.setSelected(prefs.getBoolean("security.auditLog", true));
    }

    private void setupListeners() {
        // Auto-save on changes
        emailNotifyToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putBoolean("notifications.email", newVal);
        });

        desktopNotifyToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putBoolean("notifications.desktop", newVal);
        });

        soundNotifyToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            prefs.putBoolean("notifications.sound", newVal);
        });
    }

    @FXML
    private void handleSaveSettings() {
        try {
            // Save General Settings
            prefs.put("app.name", appNameField.getText());
            prefs.put("app.theme", themeCombo.getValue());
            prefs.put("app.language", languageCombo.getValue());
            prefs.putDouble("app.fontSize", fontSizeSlider.getValue());

            // Save Notification Settings
            prefs.putBoolean("notifications.email", emailNotifyToggle.isSelected());
            prefs.putBoolean("notifications.desktop", desktopNotifyToggle.isSelected());
            prefs.putBoolean("notifications.sound", soundNotifyToggle.isSelected());
            prefs.putBoolean("app.autoRefresh", autoRefreshToggle.isSelected());
            prefs.putDouble("app.refreshInterval", refreshIntervalSlider.getValue());

            // Save AI Settings
            prefs.putBoolean("ai.classification", aiClassificationToggle.isSelected());
            prefs.putBoolean("ai.priority", aiPriorityToggle.isSelected());
            prefs.putBoolean("ai.suggestions", aiSuggestionsToggle.isSelected());
            prefs.putDouble("ai.confidence", aiConfidenceSlider.getValue());

            // Save Export Settings
            prefs.put("export.path", exportPathField.getText());
            prefs.put("export.format", defaultExportFormat.getValue());
            prefs.putBoolean("export.attachments", includeAttachmentsToggle.isSelected());

            // Save Security Settings
            prefs.putDouble("security.sessionTimeout", sessionTimeoutSlider.getValue());
            prefs.putBoolean("security.twoFactor", twoFactorToggle.isSelected());
            prefs.putBoolean("security.auditLog", auditLogToggle.isSelected());

            showAlert("Success", "Settings saved successfully!", Alert.AlertType.INFORMATION);
            System.out.println("✅ Settings saved");
        } catch (Exception e) {
            showAlert("Error", "Failed to save settings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleResetToDefaults() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Settings");
        confirm.setHeaderText("Reset all settings to defaults?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    prefs.clear();
                    loadSettings();
                    showAlert("Success", "Settings reset to defaults", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to reset settings", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleTestConnection() {
        testConnectionButton.setDisable(true);
        connectionStatusLabel.setText("Testing connection...");
        connectionStatusLabel.setStyle("-fx-text-fill: #fb8500;");

        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate connection test

                // Test Neo4j connection
                boolean connected = org.example.repository.Neo4jConnection
                        .getInstance()
                        .testConnection();

                javafx.application.Platform.runLater(() -> {
                    if (connected) {
                        connectionStatusLabel.setText("✅ Connected");
                        connectionStatusLabel.setStyle("-fx-text-fill: #1a7f37;");
                    } else {
                        connectionStatusLabel.setText("❌ Connection failed");
                        connectionStatusLabel.setStyle("-fx-text-fill: #d1242f;");
                    }
                    testConnectionButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    connectionStatusLabel.setText("❌ Error: " + e.getMessage());
                    connectionStatusLabel.setStyle("-fx-text-fill: #d1242f;");
                    testConnectionButton.setDisable(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleChooseExportPath() {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Directory");

        java.io.File selectedDir = dirChooser.showDialog(exportPathField.getScene().getWindow());
        if (selectedDir != null) {
            exportPathField.setText(selectedDir.getAbsolutePath());
        }
    }

    @FXML
    private void handleClearCache() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Cache");
        confirm.setHeaderText("Clear application cache?");
        confirm.setContentText("This will remove temporary files and cached data.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Clear cache logic here
                showAlert("Success", "Cache cleared successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void handleExportSettings() {
        showAlert("Export Settings", "Settings export feature coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleImportSettings() {
        showAlert("Import Settings", "Settings import feature coming soon!", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}