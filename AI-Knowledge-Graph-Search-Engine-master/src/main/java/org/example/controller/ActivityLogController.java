package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import org.example.model.AuditLog;
import org.example.repository.AuditRepository;
import org.example.util.DateUtils;
import org.example.service.ExportService;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Activity Log Controller - System-wide activity monitoring and audit trail
 */
public class ActivityLogController implements Initializable {

    @FXML private TableView<AuditLog> activityTable;
    @FXML private TableColumn<AuditLog, String> colTimestamp;
    @FXML private TableColumn<AuditLog, String> colAction;
    @FXML private TableColumn<AuditLog, String> colUser;
    @FXML private TableColumn<AuditLog, String> colEntity;
    @FXML private TableColumn<AuditLog, String> colDetails;
    @FXML private TableColumn<AuditLog, String> colResult;

    @FXML private ComboBox<String> actionFilterCombo;
    @FXML private ComboBox<String> userFilterCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField searchField;
    @FXML private Label totalLogsLabel;
    @FXML private Label filteredLogsLabel;

    private AuditRepository auditRepository;
    private ObservableList<AuditLog> allLogs;
    private ObservableList<AuditLog> filteredLogs;
    private ExportService exportService;

    public ActivityLogController() {
        this.auditRepository = new AuditRepository();
        this.exportService = new ExportService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupFilters();
        loadActivityLogs();
        setupAutoRefresh();
    }

    private void setupTable() {
        colTimestamp.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        DateUtils.formatDateTime(cellData.getValue().getTimestamp())
                )
        );

        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));

        colEntity.setCellValueFactory(cellData -> {
            AuditLog log = cellData.getValue();
            String entity = log.getEntityType() + ": " + log.getEntityId();
            return new javafx.beans.property.SimpleStringProperty(entity);
        });

        colDetails.setCellValueFactory(cellData -> {
            AuditLog log = cellData.getValue();
            String details = "";
            if (log.getOldValue() != null && log.getNewValue() != null) {
                details = log.getOldValue() + " → " + log.getNewValue();
            }
            return new javafx.beans.property.SimpleStringProperty(details);
        });

        colResult.setCellValueFactory(new PropertyValueFactory<>("result"));

        // Style result column
        colResult.setCellFactory(col -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String result, boolean empty) {
                super.updateItem(result, empty);
                if (empty || result == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(result);
                    if ("SUCCESS".equalsIgnoreCase(result)) {
                        setStyle("-fx-text-fill: #1a7f37; -fx-font-weight: 600;");
                    } else if ("FAILURE".equalsIgnoreCase(result)) {
                        setStyle("-fx-text-fill: #d1242f; -fx-font-weight: 600;");
                    }
                }
            }
        });

        // Action column with icons
        colAction.setCellFactory(col -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String action, boolean empty) {
                super.updateItem(action, empty);
                if (empty || action == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String icon = getActionIcon(action);
                    Label label = new Label(icon + " " + action);
                    setGraphic(label);
                    setText(null);
                }
            }
        });
    }

    private String getActionIcon(String action) {
        if (action == null) return "📝";

        String lower = action.toLowerCase();
        if (lower.contains("create")) return "➕";
        if (lower.contains("update")) return "✏️";
        if (lower.contains("delete")) return "🗑️";
        if (lower.contains("login")) return "🔐";
        if (lower.contains("logout")) return "🚪";
        if (lower.contains("view")) return "👁️";
        if (lower.contains("export")) return "📤";
        if (lower.contains("import")) return "📥";
        return "📝";
    }

    private void setupFilters() {
        actionFilterCombo.setItems(FXCollections.observableArrayList(
                "All Actions", "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "VIEW", "EXPORT"
        ));
        actionFilterCombo.setValue("All Actions");
        actionFilterCombo.setOnAction(e -> applyFilters());

        userFilterCombo.setItems(FXCollections.observableArrayList(
                "All Users", "Admin", "John Doe", "Jane Smith", "System"
        ));
        userFilterCombo.setValue("All Users");
        userFilterCombo.setOnAction(e -> applyFilters());

        startDatePicker.setValue(java.time.LocalDate.now().minusDays(7));
        startDatePicker.setOnAction(e -> applyFilters());

        endDatePicker.setValue(java.time.LocalDate.now());
        endDatePicker.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadActivityLogs() {
        try {
            List<AuditLog> logs = auditRepository.findRecent(1000);
            allLogs = FXCollections.observableArrayList(logs);
            filteredLogs = FXCollections.observableArrayList(logs);
            activityTable.setItems(filteredLogs);
            updateStatistics();
            System.out.println("✅ Loaded " + logs.size() + " activity logs");
        } catch (Exception e) {
            System.err.println("❌ Error loading activity logs: " + e.getMessage());
            allLogs = FXCollections.observableArrayList();
            filteredLogs = FXCollections.observableArrayList();
        }
    }

    private void applyFilters() {
        List<AuditLog> filtered = allLogs.stream()
                .filter(this::matchesActionFilter)
                .filter(this::matchesUserFilter)
                .filter(this::matchesDateFilter)
                .filter(this::matchesSearchFilter)
                .collect(Collectors.toList());

        filteredLogs.setAll(filtered);
        updateStatistics();
    }

    private boolean matchesActionFilter(AuditLog log) {
        String filter = actionFilterCombo.getValue();
        if ("All Actions".equals(filter)) return true;
        return log.getAction() != null && log.getAction().toUpperCase().contains(filter);
    }

    private boolean matchesUserFilter(AuditLog log) {
        String filter = userFilterCombo.getValue();
        if ("All Users".equals(filter)) return true;
        return log.getUsername() != null && log.getUsername().contains(filter);
    }

    private boolean matchesDateFilter(AuditLog log) {
        if (log.getTimestamp() == null) return true;

        LocalDateTime start = startDatePicker.getValue().atStartOfDay();
        LocalDateTime end = endDatePicker.getValue().atTime(23, 59, 59);

        return !log.getTimestamp().isBefore(start) && !log.getTimestamp().isAfter(end);
    }

    private boolean matchesSearchFilter(AuditLog log) {
        String search = searchField.getText();
        if (search == null || search.trim().isEmpty()) return true;

        String searchLower = search.toLowerCase();
        return (log.getAction() != null && log.getAction().toLowerCase().contains(searchLower)) ||
                (log.getUsername() != null && log.getUsername().toLowerCase().contains(searchLower)) ||
                (log.getEntityType() != null && log.getEntityType().toLowerCase().contains(searchLower)) ||
                (log.getEntityId() != null && log.getEntityId().toLowerCase().contains(searchLower));
    }

    private void updateStatistics() {
        totalLogsLabel.setText(String.valueOf(allLogs.size()));
        filteredLogsLabel.setText(String.valueOf(filteredLogs.size()));
    }

    private void setupAutoRefresh() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.minutes(2),
                        e -> {
                            System.out.println("🔄 Auto-refreshing activity logs...");
                            loadActivityLogs();
                        }
                )
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refreshing activity logs...");
        loadActivityLogs();
        applyFilters();
    }

    @FXML
    private void handleExport() {
        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Directory");

        java.io.File selectedDir = dirChooser.showDialog(activityTable.getScene().getWindow());
        if (selectedDir != null) {
            try {
                // Create CSV export
                StringBuilder csv = new StringBuilder();
                csv.append("Timestamp,Action,User,Entity Type,Entity ID,Old Value,New Value,Result\n");

                for (AuditLog log : filteredLogs) {
                    csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            DateUtils.formatDateTime(log.getTimestamp()),
                            log.getAction(),
                            log.getUsername(),
                            log.getEntityType(),
                            log.getEntityId(),
                            log.getOldValue(),
                            log.getNewValue(),
                            log.getResult()
                    ));
                }

                String filename = "activity_log_" +
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) +
                        ".csv";

                java.nio.file.Files.write(
                        java.nio.file.Paths.get(selectedDir.getAbsolutePath(), filename),
                        csv.toString().getBytes()
                );

                showAlert("Success", "Activity log exported to: " + filename, Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to export: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClearFilters() {
        actionFilterCombo.setValue("All Actions");
        userFilterCombo.setValue("All Users");
        startDatePicker.setValue(java.time.LocalDate.now().minusDays(7));
        endDatePicker.setValue(java.time.LocalDate.now());
        searchField.clear();
        applyFilters();
    }

    @FXML
    private void handleViewDetails() {
        AuditLog selected = activityTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select an activity log entry", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Activity Log Details");
        alert.setHeaderText("Log Entry Details");

        StringBuilder details = new StringBuilder();
        details.append("Timestamp: ").append(DateUtils.formatDateTime(selected.getTimestamp())).append("\n");
        details.append("Action: ").append(selected.getAction()).append("\n");
        details.append("User: ").append(selected.getUsername()).append(" (").append(selected.getUserId()).append(")\n");
        details.append("Entity: ").append(selected.getEntityType()).append(" - ").append(selected.getEntityId()).append("\n");
        if (selected.getOldValue() != null) {
            details.append("Old Value: ").append(selected.getOldValue()).append("\n");
        }
        if (selected.getNewValue() != null) {
            details.append("New Value: ").append(selected.getNewValue()).append("\n");
        }
        if (selected.getIpAddress() != null) {
            details.append("IP Address: ").append(selected.getIpAddress()).append("\n");
        }
        details.append("Result: ").append(selected.getResult()).append("\n");

        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(12);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteOldLogs() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Old Logs");
        confirm.setHeaderText("Delete logs older than 90 days?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
                    auditRepository.deleteOlderThan(cutoff);
                    loadActivityLogs();
                    showAlert("Success", "Old logs deleted successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete logs: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}