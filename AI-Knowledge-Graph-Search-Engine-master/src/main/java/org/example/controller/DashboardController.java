package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import org.example.service.AnalyticsService;
import org.example.repository.TicketRepository;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DashboardController implements Initializable {

    @FXML private Label totalTicketsLabel;
    @FXML private Label openTicketsLabel;
    @FXML private Label resolvedTicketsLabel;
    @FXML private Label avgResolutionLabel;
    @FXML private Label slaComplianceLabel;
    @FXML private Label criticalTicketsLabel;

    @FXML private PieChart statusPieChart;
    @FXML private BarChart<String, Number> priorityBarChart;
    @FXML private LineChart<String, Number> trendLineChart;
    @FXML private BarChart<String, Number> categoryBarChart;

    private final AnalyticsService analyticsService;
    private final TicketRepository ticketRepository;

    public DashboardController() {
        this.analyticsService = new AnalyticsService();
        this.ticketRepository = new TicketRepository();
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

            totalTicketsLabel.setText(String.valueOf(metrics.get("totalTickets")));
            openTicketsLabel.setText(String.valueOf(metrics.get("openTickets")));
            resolvedTicketsLabel.setText(String.valueOf(metrics.get("resolvedTickets")));

            double avgTime = (Double) metrics.getOrDefault("averageResolutionTime", 0.0);
            avgResolutionLabel.setText(String.format("%.1fh", avgTime / 60));

            double slaCompliance = (Double) metrics.getOrDefault("slaComplianceRate", 0.0);
            slaComplianceLabel.setText(String.format("%.1f%%", slaCompliance));

            long criticalCount = ticketRepository.countByPriority(
                    org.example.model.enums.Priority.CRITICAL
            );
            criticalTicketsLabel.setText(String.valueOf(criticalCount));

            System.out.println("✅ Dashboard metrics loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Error loading dashboard metrics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCharts() {
        if (statusPieChart != null) {
            statusPieChart.setTitle("Tickets by Status");
            statusPieChart.setLegendVisible(true);
            statusPieChart.setLabelsVisible(true);
        }

        if (priorityBarChart != null) {
            priorityBarChart.setTitle("Tickets by Priority");
            priorityBarChart.setLegendVisible(false);
        }

        if (trendLineChart != null) {
            trendLineChart.setTitle("Ticket Trend (Last 7 Days)");
            trendLineChart.setLegendVisible(true);
        }

        if (categoryBarChart != null) {
            categoryBarChart.setTitle("Top Categories");
            categoryBarChart.setLegendVisible(false);
        }
    }

    private void loadChartData() {
        loadStatusChart();
        loadPriorityChart();
        loadTrendChart();
        loadCategoryChart();
    }

    private void loadStatusChart() {
        if (statusPieChart == null) return;

        try {
            Map<String, Long> statusCounts = analyticsService.getTicketsByStatus();
            statusPieChart.getData().clear();

            statusCounts.forEach((status, count) -> {
                if (count > 0) {
                    PieChart.Data slice = new PieChart.Data(
                            status + " (" + count + ")",
                            count
                    );
                    statusPieChart.getData().add(slice);
                }
            });

            // Apply colors to pie chart slices
            applyPieChartColors();

        } catch (Exception e) {
            System.err.println("❌ Error loading status chart: " + e.getMessage());
        }
    }

    private void applyPieChartColors() {
        if (statusPieChart == null) return;

        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("OPEN", "#0969da");
        colorMap.put("IN_PROGRESS", "#0969da");
        colorMap.put("RESOLVED", "#1a7f37");
        colorMap.put("CLOSED", "#57606a");
        colorMap.put("CANCELLED", "#d1242f");

        statusPieChart.getData().forEach(data -> {
            String status = data.getName().split(" ")[0].toUpperCase();
            String color = colorMap.getOrDefault(status, "#6e7781");
            data.getNode().setStyle("-fx-pie-color: " + color + ";");
        });
    }

    private void loadPriorityChart() {
        if (priorityBarChart == null) return;

        try {
            Map<String, Long> priorityCounts = analyticsService.getTicketsByPriority();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tickets");

            // Order by priority level
            String[] priorities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
            for (String priority : priorities) {
                Long count = priorityCounts.getOrDefault(priority, 0L);
                series.getData().add(new XYChart.Data<>(priority, count));
            }

            priorityBarChart.getData().clear();
            priorityBarChart.getData().add(series);

            // Apply colors to bars
            applyPriorityColors();

        } catch (Exception e) {
            System.err.println("❌ Error loading priority chart: " + e.getMessage());
        }
    }

    private void applyPriorityColors() {
        if (priorityBarChart == null) return;

        priorityBarChart.lookupAll(".data0.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #2da44e;")
        );
        priorityBarChart.lookupAll(".data1.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #0969da;")
        );
        priorityBarChart.lookupAll(".data2.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #fb8500;")
        );
        priorityBarChart.lookupAll(".data3.chart-bar").forEach(node ->
                node.setStyle("-fx-bar-fill: #d1242f;")
        );
    }

    private void loadTrendChart() {
        if (trendLineChart == null) return;

        try {
            List<Map<String, Object>> trendData = analyticsService.getTicketTrend(7);

            XYChart.Series<String, Number> createdSeries = new XYChart.Series<>();
            createdSeries.setName("Created");

            XYChart.Series<String, Number> resolvedSeries = new XYChart.Series<>();
            resolvedSeries.setName("Resolved");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(formatter);

                // Simulated data - replace with actual data from trendData
                int created = (int)(Math.random() * 10) + 5;
                int resolved = (int)(Math.random() * 8) + 3;

                createdSeries.getData().add(new XYChart.Data<>(dateStr, created));
                resolvedSeries.getData().add(new XYChart.Data<>(dateStr, resolved));
            }

            trendLineChart.getData().clear();
            trendLineChart.getData().addAll(createdSeries, resolvedSeries);

        } catch (Exception e) {
            System.err.println("❌ Error loading trend chart: " + e.getMessage());
        }
    }

    private void loadCategoryChart() {
        if (categoryBarChart == null) return;

        try {
            Map<String, Long> categoryCounts = analyticsService.getTicketsByCategory();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tickets");

            // Get top 5 categories
            categoryCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    });

            categoryBarChart.getData().clear();
            categoryBarChart.getData().add(series);

        } catch (Exception e) {
            System.err.println("❌ Error loading category chart: " + e.getMessage());
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
        // Implement export functionality
    }

    @FXML
    private void handlePrintReport() {
        System.out.println("🖨️ Printing dashboard report...");
        // Implement print functionality
    }

    @FXML
    private void handleEmailReport() {
        System.out.println("📧 Emailing dashboard report...");
        // Implement email functionality
    }
}