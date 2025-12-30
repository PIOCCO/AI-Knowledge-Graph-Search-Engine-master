package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.example.service.ReportService;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.stage.FileChooser;

/**
 * Report Controller - Handles generation and exporting of system reports
 */
public class ReportController implements Initializable {

    @FXML
    private ComboBox<String> reportTypeCombo;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private TextArea reportPreview;
    @FXML
    private ProgressBar generationProgress;

    private final ReportService reportService;

    public ReportController() {
        this.reportService = new ReportService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupReportTypes();
        generationProgress.setVisible(false);
    }

    private void setupReportTypes() {
        reportTypeCombo.getItems().addAll(
                "Ticket Summary Report",
                "SLA Compliance Report",
                "Agent Performance Report",
                "Customer Satisfaction Survey",
                "System Audit Log");
        reportTypeCombo.setValue("Ticket Summary Report");
    }

    @FXML
    private void handleGeneratePreview() {
        String type = reportTypeCombo.getValue();
        generationProgress.setVisible(true);
        generationProgress.setProgress(-1);

        // Simulation of generation
        new Thread(() -> {
            try {
                String result;
                if (type.contains("Ticket"))
                    result = reportService.generateTicketReport();
                else if (type.contains("SLA"))
                    result = reportService.generateSLAReport();
                else if (type.contains("Agent"))
                    result = reportService.generateAgentPerformanceReport();
                else
                    result = "Report generation for " + type + " started...";

                javafx.application.Platform.runLater(() -> {
                    reportPreview.setText(result);
                    generationProgress.setVisible(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    reportPreview.setText("Error: " + e.getMessage());
                    generationProgress.setVisible(false);
                });
            }
        }).start();
    }

    @FXML
    private void handleExportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName(reportTypeCombo.getValue().replace(" ", "_").toLowerCase() + ".csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                reportService.exportReportToCSV(file.getAbsolutePath(), "DATA_FOR_" + reportTypeCombo.getValue());
                showAlert("Success", "Report exported successfully to: " + file.getName());
            } catch (Exception e) {
                showAlert("Error", "Failed to export report: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrint() {
        showAlert("Print", "Sending report to system printer...");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
