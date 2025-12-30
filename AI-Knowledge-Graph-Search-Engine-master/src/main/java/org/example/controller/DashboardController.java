package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.service.AnalyticsService;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dashboard Controller - Main dashboard with metrics and charts
 */
public class DashboardController implements Initializable {

    @FXML
    private Label totalTicketsLabel;
    @FXML
    private Label openTicketsLabel;
    @FXML
    private Label resolvedTicketsLabel;
    @FXML
    private Label slaComplianceLabel;

    @FXML
    private PieChart statusPieChart;
    @FXML
    private BarChart<String, Number> priorityBarChart;
    @FXML
    private LineChart<String, Number> trendLineChart;
    @FXML
    private VBox metricsContainer;

    private final AnalyticsService analyticsService;

    public DashboardController() {
        this.analyticsService = new AnalyticsService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadDashboardMetrics();
        setupCharts();
        loadChartData();
    }

    private void loadDashboardMetrics() {
        try {
            Map<String, Object> metrics = analyticsService.getDashboardMetrics();

            // Update metric labels
            if (totalTicketsLabel != null) {
                totalTicketsLabel.setText(String.valueOf(metrics.get("totalTickets")));
            }
            if (openTicketsLabel != null) {
                openTicketsLabel.setText(String.valueOf(metrics.get("openTickets")));
            }
            if (resolvedTicketsLabel != null) {
                resolvedTicketsLabel.setText(String.valueOf(metrics.get("resolvedTickets")));
            }
            if (slaComplianceLabel != null) {
                double compliance = (Double) metrics.get("slaComplianceRate");
                slaComplianceLabel.setText(String.format("%.1f%%", compliance));
            }

            System.out.println("✅ Dashboard metrics loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Error loading dashboard metrics: " + e.getMessage());
        }
    }

    private void setupCharts() {
        // Setup Status Pie Chart
        if (statusPieChart != null) {
            statusPieChart.setTitle("Tickets by Status");
            statusPieChart.setLegendVisible(true);
        }

        // Setup Priority Bar Chart
        if (priorityBarChart != null) {
            priorityBarChart.setTitle("Tickets by Priority");
            priorityBarChart.setLegendVisible(false);
        }

        // Setup Trend Line Chart
        if (trendLineChart != null) {
            trendLineChart.setTitle("Ticket Trend (Last 7 Days)");
            trendLineChart.setLegendVisible(true);
        }
    }

    private void loadChartData() {
        loadStatusChart();
        loadPriorityChart();
        loadTrendChart();
    }

    private void loadStatusChart() {
        if (statusPieChart == null)
            return;

        try {
            Map<String, Long> statusCounts = analyticsService.getTicketsByStatus();

            statusPieChart.getData().clear();
            statusCounts.forEach((status, count) -> {
                if (count > 0) {
                    PieChart.Data data = new PieChart.Data(status + " (" + count + ")", count);
                    statusPieChart.getData().add(data);
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Error loading status chart: " + e.getMessage());
        }
    }

    private void loadPriorityChart() {
        if (priorityBarChart == null)
            return;

        try {
            Map<String, Long> priorityCounts = analyticsService.getTicketsByPriority();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tickets");

            priorityCounts.forEach((priority, count) -> {
                series.getData().add(new XYChart.Data<>(priority, count));
            });

            priorityBarChart.getData().clear();
            priorityBarChart.getData().add(series);
        } catch (Exception e) {
            System.err.println("❌ Error loading priority chart: " + e.getMessage());
        }
    }

    private void loadTrendChart() {
        if (trendLineChart == null)
            return;

        try {
            var trendData = analyticsService.getTicketTrend(7);

            XYChart.Series<String, Number> createdSeries = new XYChart.Series<>();
            createdSeries.setName("Created");

            XYChart.Series<String, Number> resolvedSeries = new XYChart.Series<>();
            resolvedSeries.setName("Resolved");

            for (Map<String, Object> dayData : trendData) {
                String date = (String) dayData.get("date");
                int created = (Integer) dayData.get("created");
                int resolved = (Integer) dayData.get("resolved");

                createdSeries.getData().add(new XYChart.Data<>(date, created));
                resolvedSeries.getData().add(new XYChart.Data<>(date, resolved));
            }

            trendLineChart.getData().clear();
            @SuppressWarnings("unchecked")
            XYChart.Series<String, Number>[] seriesArray = new XYChart.Series[] { createdSeries, resolvedSeries };
            trendLineChart.getData().addAll(seriesArray);
        } catch (Exception e) {
            System.err.println("❌ Error loading trend chart: " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadDashboardMetrics();
        loadChartData();
        System.out.println("✅ Dashboard refreshed");
    }

    @FXML
    private void handleExport() {
        System.out.println("📊 Exporting dashboard data...");
        // Export logic would go here
    }
}
