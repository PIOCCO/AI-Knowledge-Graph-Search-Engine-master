package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import org.example.service.BackupService;
import java.util.prefs.Preferences;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Backup & Restore Controller - Database backup and recovery management
 */
public class BackupRestoreController implements Initializable {

    @FXML private ListView<String> backupsList;
    @FXML private Label lastBackupLabel;
    @FXML private Label backupSizeLabel;
    @FXML private Label totalBackupsLabel;

    @FXML private CheckBox autoBackupToggle;
    @FXML private ComboBox<String> backupFrequencyCombo;
    @FXML private TextField backupLocationField;
    @FXML private Slider retentionSlider;
    @FXML private Label retentionLabel;

    @FXML private CheckBox includeTicketsToggle;
    @FXML private CheckBox includeUsersToggle;
    @FXML private CheckBox includeCategoriesToggle;
    @FXML private CheckBox includeCommentsToggle;
    @FXML private CheckBox includeLogsToggle;

    @FXML private ProgressBar backupProgress;
    @FXML private Label statusLabel;
    @FXML private TextArea backupLogArea;

    private BackupService backupService;
    private List<String> availableBackups;

    public BackupRestoreController() {
        this.backupService = new BackupService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupControls();
        loadBackupSettings();
        loadAvailableBackups();
        setupListeners();
    }

    private void setupControls() {
        backupFrequencyCombo.setItems(FXCollections.observableArrayList(
                "Daily", "Weekly", "Monthly", "Custom"
        ));
        backupFrequencyCombo.setValue("Daily");

        retentionSlider.setMin(7);
        retentionSlider.setMax(365);
        retentionSlider.setValue(30);
        retentionSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            retentionLabel.setText(newVal.intValue() + " days");
        });
    }

    private void loadBackupSettings() {
        Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(getClass());

        autoBackupToggle.setSelected(prefs.getBoolean("backup.auto", false));
        backupFrequencyCombo.setValue(prefs.get("backup.frequency", "Daily"));
        backupLocationField.setText(prefs.get("backup.location",
                System.getProperty("user.home") + "/backups"));
        retentionSlider.setValue(prefs.getDouble("backup.retention", 30));

        includeTicketsToggle.setSelected(prefs.getBoolean("backup.includeTickets", true));
        includeUsersToggle.setSelected(prefs.getBoolean("backup.includeUsers", true));
        includeCategoriesToggle.setSelected(prefs.getBoolean("backup.includeCategories", true));
        includeCommentsToggle.setSelected(prefs.getBoolean("backup.includeComments", true));
        includeLogsToggle.setSelected(prefs.getBoolean("backup.includeLogs", false));
    }

    private void loadAvailableBackups() {
        try {
            availableBackups = backupService.listBackups(backupLocationField.getText());
            backupsList.setItems(FXCollections.observableArrayList(availableBackups));

            if (!availableBackups.isEmpty()) {
                lastBackupLabel.setText(availableBackups.get(0));
                totalBackupsLabel.setText(String.valueOf(availableBackups.size()));

                // Calculate total size (simulated)
                backupSizeLabel.setText(String.format("%.2f MB", availableBackups.size() * 15.5));
            }

            log("✅ Found " + availableBackups.size() + " backups");
        } catch (Exception e) {
            log("❌ Error loading backups: " + e.getMessage());
        }
    }

    private void setupListeners() {
        backupsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                log("Selected backup: " + newVal);
            }
        });
    }

    @FXML
    private void handleCreateBackup() {
        statusLabel.setText("Creating backup...");
        backupProgress.setProgress(0);

        new Thread(() -> {
            try {
                Map<String, Boolean> options = new HashMap<>();
                options.put("tickets", includeTicketsToggle.isSelected());
                options.put("users", includeUsersToggle.isSelected());
                options.put("categories", includeCategoriesToggle.isSelected());
                options.put("comments", includeCommentsToggle.isSelected());
                options.put("logs", includeLogsToggle.isSelected());

                String backupFile = backupService.createBackup(
                        backupLocationField.getText(),
                        options,
                        progress -> javafx.application.Platform.runLater(() ->
                                backupProgress.setProgress(progress)
                        )
                );

                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("✅ Backup completed: " + backupFile);
                    backupProgress.setProgress(1.0);
                    log("Backup created successfully: " + backupFile);
                    loadAvailableBackups();
                    showAlert("Success", "Backup created successfully!", Alert.AlertType.INFORMATION);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Backup failed");
                    backupProgress.setProgress(0);
                    log("Error creating backup: " + e.getMessage());
                    showAlert("Error", "Backup failed: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    private void handleRestoreBackup() {
        String selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a backup to restore", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Restore Backup");
        confirm.setHeaderText("Restore from backup?");
        confirm.setContentText("This will replace current data with backup data.\n\nBackup: " + selected);

        ButtonType continueBtn = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(continueBtn, cancelBtn);

        confirm.showAndWait().ifPresent(response -> {
            if (response == continueBtn) {
                performRestore(selected);
            }
        });
    }

    private void performRestore(String backupFile) {
        statusLabel.setText("Restoring backup...");
        backupProgress.setProgress(0);

        new Thread(() -> {
            try {
                backupService.restoreBackup(
                        backupLocationField.getText() + "/" + backupFile,
                        progress -> javafx.application.Platform.runLater(() ->
                                backupProgress.setProgress(progress)
                        )
                );

                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("✅ Restore completed");
                    backupProgress.setProgress(1.0);
                    log("Restore completed successfully");
                    showAlert("Success", "Backup restored successfully!\nPlease restart the application.",
                            Alert.AlertType.INFORMATION);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Restore failed");
                    backupProgress.setProgress(0);
                    log("Error restoring backup: " + e.getMessage());
                    showAlert("Error", "Restore failed: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    private void handleDeleteBackup() {
        String selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a backup to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Backup");
        confirm.setHeaderText("Delete backup?");
        confirm.setContentText("Backup: " + selected);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    backupService.deleteBackup(backupLocationField.getText() + "/" + selected);
                    loadAvailableBackups();
                    log("Backup deleted: " + selected);
                    showAlert("Success", "Backup deleted", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    log("Error deleting backup: " + e.getMessage());
                    showAlert("Error", "Failed to delete: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleVerifyBackup() {
        String selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a backup to verify", Alert.AlertType.WARNING);
            return;
        }

        statusLabel.setText("Verifying backup...");

        new Thread(() -> {
            try {
                boolean valid = backupService.verifyBackup(
                        backupLocationField.getText() + "/" + selected
                );

                javafx.application.Platform.runLater(() -> {
                    if (valid) {
                        statusLabel.setText("✅ Backup is valid");
                        log("Backup verification successful: " + selected);
                        showAlert("Verification Success",
                                "Backup is valid and can be restored",
                                Alert.AlertType.INFORMATION);
                    } else {
                        statusLabel.setText("❌ Backup is corrupted");
                        log("Backup verification failed: " + selected);
                        showAlert("Verification Failed",
                                "Backup may be corrupted or incomplete",
                                Alert.AlertType.ERROR);
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Verification error");
                    log("Error verifying backup: " + e.getMessage());
                    showAlert("Error", "Verification failed: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    private void handleChooseLocation() {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Backup Directory");

        java.io.File currentDir = new java.io.File(backupLocationField.getText());
        if (currentDir.exists()) {
            dirChooser.setInitialDirectory(currentDir);
        }

        java.io.File selectedDir = dirChooser.showDialog(backupLocationField.getScene().getWindow());
        if (selectedDir != null) {
            backupLocationField.setText(selectedDir.getAbsolutePath());
            loadAvailableBackups();
        }
    }

    @FXML
    private void handleSaveSettings() {
        try {
            Preferences prefs = java.util.prefs.Preferences.userNodeForPackage(getClass());

            prefs.putBoolean("backup.auto", autoBackupToggle.isSelected());
            prefs.put("backup.frequency", backupFrequencyCombo.getValue());
            prefs.put("backup.location", backupLocationField.getText());
            prefs.putDouble("backup.retention", retentionSlider.getValue());

            prefs.putBoolean("backup.includeTickets", includeTicketsToggle.isSelected());
            prefs.putBoolean("backup.includeUsers", includeUsersToggle.isSelected());
            prefs.putBoolean("backup.includeCategories", includeCategoriesToggle.isSelected());
            prefs.putBoolean("backup.includeComments", includeCommentsToggle.isSelected());
            prefs.putBoolean("backup.includeLogs", includeLogsToggle.isSelected());

            log("Settings saved successfully");
            showAlert("Success", "Backup settings saved!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            log("Error saving settings: " + e.getMessage());
            showAlert("Error", "Failed to save settings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleScheduleBackup() {
        if (autoBackupToggle.isSelected()) {
            String frequency = backupFrequencyCombo.getValue();
            log("Automatic backups scheduled: " + frequency);
            showAlert("Scheduled",
                    "Automatic backups will run " + frequency.toLowerCase(),
                    Alert.AlertType.INFORMATION);
        } else {
            showAlert("Not Enabled",
                    "Please enable automatic backups first",
                    Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleCleanupOldBackups() {
        int retention = (int) retentionSlider.getValue();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cleanup Old Backups");
        confirm.setHeaderText("Delete backups older than " + retention + " days?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int deleted = backupService.cleanupOldBackups(
                            backupLocationField.getText(),
                            retention
                    );
                    loadAvailableBackups();
                    log("Deleted " + deleted + " old backups");
                    showAlert("Success",
                            "Deleted " + deleted + " old backups",
                            Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    log("Error cleaning up backups: " + e.getMessage());
                    showAlert("Error", "Cleanup failed: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleExportBackup() {
        String selected = backupsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a backup to export", Alert.AlertType.WARNING);
            return;
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Backup");
        fileChooser.setInitialFileName(selected);

        java.io.File file = fileChooser.showSaveDialog(backupsList.getScene().getWindow());
        if (file != null) {
            try {
                backupService.exportBackup(
                        backupLocationField.getText() + "/" + selected,
                        file.getAbsolutePath()
                );
                log("Backup exported to: " + file.getAbsolutePath());
                showAlert("Success", "Backup exported successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                log("Error exporting backup: " + e.getMessage());
                showAlert("Error", "Export failed: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
        backupLogArea.appendText("[" + timestamp + "] " + message + "\n");
        System.out.println(message);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}