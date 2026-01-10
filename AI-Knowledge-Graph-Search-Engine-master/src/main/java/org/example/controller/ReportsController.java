package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import org.example.service.ReportService;
import org.example.service.ExportService;
import org.example.model.Report;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;

/**
 * Reports Controller - Generate comprehensive system reports
 */
public class ReportsController implements Initializable {

    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> formatCombo;
    @FXML private CheckBox includeChartsToggle;
    @FXML private CheckBox includeSummaryToggle;
    @FXML private CheckBox includeDetailsToggle;
    @FXML private TextArea reportPreview;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;
    @FXML private Button generateButton;
    @FXML private Button exportButton;
    @FXML private ListView<String> savedReportsList;

    private ReportService reportService;
    private ExportService exportService;
    private Report currentReport;

    public ReportsController() {
        this.reportService = new ReportService();
        this.exportService = new ExportService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupDatePickers();
        loadSavedReports();
        setupListeners();
    }

    private void setupComboBoxes() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Ticket Summary Report",
                "Agent Performance Report",
                "SLA Compliance Report",
                "Category Analysis Report",
                "Customer Satisfaction Report",
                "Resolution Time Report",
                "Trend Analysis Report",
                "Custom Report"
        ));
        reportTypeCombo.setValue("Ticket Summary Report");

        formatCombo.setItems(FXCollections.observableArrayList(
                "PDF", "HTML", "Excel", "CSV", "JSON"
        ));
        formatCombo.setValue("PDF");
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
    }

    private void loadSavedReports() {
        List<String> savedReports = reportService.getSavedReportNames();
        savedReportsList.setItems(FXCollections.observableArrayList(savedReports));
    }

    private void setupListeners() {
        reportTypeCombo.setOnAction(e -> updateReportPreview());
        startDatePicker.setOnAction(e -> updateReportPreview());
        endDatePicker.setOnAction(e -> updateReportPreview());
    }

    @FXML
    private void handleGenerateReport() {
        String reportType = reportTypeCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate.isAfter(endDate)) {
            showAlert("Invalid Date Range", "Start date must be before end date", Alert.AlertType.WARNING);
            return;
        }

        generateButton.setDisable(true);
        statusLabel.setText("Generating report...");
        progressBar.setProgress(0);

        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i += 10) {
                    final int progress = i;
                    javafx.application.Platform.runLater(() -> progressBar.setProgress(progress / 100.0));
                    Thread.sleep(200);
                }

                currentReport = reportService.generateReport(
                        reportType,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59),
                        includeChartsToggle.isSelected(),
                        includeSummaryToggle.isSelected(),
                        includeDetailsToggle.isSelected()
                );

                javafx.application.Platform.runLater(() -> {
                    reportPreview.setText(currentReport.getPreviewText());
                    statusLabel.setText("✅ Report generated successfully!");
                    progressBar.setProgress(1.0);
                    generateButton.setDisable(false);
                    exportButton.setDisable(false);
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("❌ Error: " + e.getMessage());
                    progressBar.setProgress(0);
                    generateButton.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleExportReport() {
        if (currentReport == null) {
            showAlert("No Report", "Please generate a report first", Alert.AlertType.WARNING);
            return;
        }

        javafx.stage.DirectoryChooser dirChooser = new javafx.stage.DirectoryChooser();
        dirChooser.setTitle("Select Export Directory");

        java.io.File selectedDir = dirChooser.showDialog(generateButton.getScene().getWindow());
        if (selectedDir != null) {
            try {
                String format = formatCombo.getValue();
                String filename = reportService.exportReport(
                        currentReport,
                        format,
                        selectedDir.getAbsolutePath()
                );

                showAlert("Success", "Report exported to: " + filename, Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to export: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleSaveReport() {
        if (currentReport == null) {
            showAlert("No Report", "Please generate a report first", Alert.AlertType.WARNING);
            return;
        }

        TextInputDialog dialog = new TextInputDialog("My Report");
        dialog.setTitle("Save Report");
        dialog.setHeaderText("Save Report Template");
        dialog.setContentText("Report Name:");

        dialog.showAndWait().ifPresent(name -> {
            try {
                reportService.saveReport(currentReport, name);
                loadSavedReports();
                showAlert("Success", "Report saved successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to save: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleLoadSavedReport() {
        String selectedReport = savedReportsList.getSelectionModel().getSelectedItem();
        if (selectedReport != null) {
            try {
                currentReport = reportService.loadReport(selectedReport);
                reportPreview.setText(currentReport.getPreviewText());
                statusLabel.setText("✅ Report loaded: " + selectedReport);
            } catch (Exception e) {
                showAlert("Error", "Failed to load report: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleDeleteSavedReport() {
        String selectedReport = savedReportsList.getSelectionModel().getSelectedItem();
        if (selectedReport != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Report");
            confirm.setHeaderText("Delete saved report?");
            confirm.setContentText("Report: " + selectedReport);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        reportService.deleteReport(selectedReport);
                        loadSavedReports();
                        showAlert("Success", "Report deleted", Alert.AlertType.INFORMATION);
                    } catch (Exception e) {
                        showAlert("Error", "Failed to delete: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        }
    }

    @FXML
    private void handleScheduleReport() {
        showAlert("Schedule Report", "Report scheduling feature coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleEmailReport() {
        if (currentReport == null) {
            showAlert("No Report", "Please generate a report first", Alert.AlertType.WARNING);
            return;
        }

        showAlert("Email Report", "Email report feature coming soon!", Alert.AlertType.INFORMATION);
    }

    private void updateReportPreview() {
        String reportType = reportTypeCombo.getValue();
        reportPreview.setText("Report Preview:\n\n" +
                "Type: " + reportType + "\n" +
                "Date Range: " + startDatePicker.getValue() + " to " + endDatePicker.getValue() + "\n" +
                "Include Charts: " + includeChartsToggle.isSelected() + "\n" +
                "Include Summary: " + includeSummaryToggle.isSelected() + "\n" +
                "Include Details: " + includeDetailsToggle.isSelected() + "\n\n" +
                "Click 'Generate Report' to create the full report.");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}