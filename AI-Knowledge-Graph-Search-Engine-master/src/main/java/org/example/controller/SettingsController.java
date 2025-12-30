package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.config.AppConfig;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Settings Controller - Global application configuration and user preferences
 */
public class SettingsController implements Initializable {

    @FXML
    private TextField appNameField;
    @FXML
    private ComboBox<String> themeSelector;
    @FXML
    private CheckBox emailNotifyToggle;
    @FXML
    private Slider sessionTimeSlider;
    @FXML
    private Label sessionTimeLabel;

    private final AppConfig appConfig;

    public SettingsController() {
        this.appConfig = AppConfig.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadSettings();

        sessionTimeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sessionTimeLabel.setText(newVal.intValue() + " min");
        });
    }

    private void loadSettings() {
        appNameField.setText(appConfig.getAppName());
        themeSelector.getItems().addAll("Light", "Dark", "High Contrast");
        themeSelector.setValue(appConfig.getTheme());

        int sessionTimeout = appConfig.getSessionTimeout() / 60;
        sessionTimeSlider.setValue(sessionTimeout);
        sessionTimeLabel.setText(sessionTimeout + " min");
    }

    @FXML
    private void handleSave() {
        appConfig.setProperty("app.name", appNameField.getText());
        appConfig.setTheme(themeSelector.getValue());
        appConfig.setProperty("app.sessionTimeout", String.valueOf((int) sessionTimeSlider.getValue() * 60));

        appConfig.saveConfig();
        showAlert("Success", "Settings saved successfully!");
    }

    @FXML
    private void handleReset() {
        loadSettings();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
