package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import org.example.service.AnalyticsService;
import org.example.repository.TicketRepository;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Advanced Statistics Dashboard with real-time graphs
 */
public class StatsViewController implements Initializable {

    @FXML private Label totalTicketsLabel;
    @FXML private Label avgResolutionLabel;
    @FXML private Label todayTicketsLabel;
    @FXML private Label slaBreachLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label closedTodayLabel;

    // Charts
    @FXML private LineChart<String, Number> ticketTrendChart;
    @FXML private PieChart statusDistributionChart;
    @FXML private BarChart<String, Number> priorityDistributionChart;
    @FXML private AreaChart<String, Number> resolutionTimeChart;
    @FXML private BarChart<String, Number> categoryPerformanceChart;
    @FXML private PieChart agentWorkloadChart;
    @FXML private LineChart<String, Number> slaComplianceChart;
    @FXML private StackedBarChart<String, Number> weeklyActivityChart;

    @FXML private ComboBox<String> timeRangeCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button refreshButton;
    @FXML private Button exportButton;
    @FXML private ProgressIndicator loadingIndicator;

    private final AnalyticsService analyticsService;
    private final TicketRepository ticketRepository;
    private Timeline autoRefreshTimeline;

    public StatsViewController() {
        this.analyticsService = new AnalyticsService();
        this.ticketRepository = new TicketRepository();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTimeRangeCombo();
        setupAutoRefresh();
        loadAllStatistics();
        setupDatePickers();
    }

    private void setupTimeRangeCombo() {
        timeRangeCombo.setItems(FXCollections.observableArrayList(
                "Today", "Last 7 Days", "Last 30 Days", "Last 90 Days", "This Year", "Custom"
        ));
        timeRangeCombo.setValue("Last 30 Days");
        timeRangeCombo.setOnAction(e -> handleTimeRangeChange());
    }

    private void setupDatePickers() {
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setOnAction(e -> loadAllStatistics());
        endDatePicker.setOnAction(e -> loadAllStatistics());
    }

    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> {
            System.out.println("🔄 Auto-refreshing statistics...");
            loadAllStatistics();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private void loadAllStatistics() {
        showLoading(true);

        new Thread(() -> {
            try {
                loadKPIs();
                loadTicketTrendChart();
                loadStatusDistributionChart();
                loadPriorityDistributionChart();
                loadResolutionTimeChart();
                loadCategoryPerformanceChart();
                loadAgentWorkloadChart();
                loadSLAComplianceChart();
                loadWeeklyActivityChart();

                javafx.application.Platform.runLater(() -> {
                    showLoading(false);
                    System.out.println("✅ Statistics loaded successfully");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showLoading(false);
                    showAlert("Error", "Failed to load statistics: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadKPIs() {
        try {
            Map<String, Object> metrics = analyticsService.getDashboardMetrics();

            javafx.application.Platform.runLater(() -> {
                totalTicketsLabel.setText(String.valueOf(metrics.get("totalTickets")));

                double avgTime = (Double) metrics.getOrDefault("averageResolutionTime", 0.0);
                avgResolutionLabel.setText(String.format("%.1fh", avgTime / 60));

                // Today's tickets (simulated)
                todayTicketsLabel.setText(String.valueOf((int)(Math.random() * 20) + 5));

                // SLA breaches (simulated)
                slaBreachLabel.setText(String.valueOf((int)(Math.random() * 3)));

                // Active users (simulated)
                activeUsersLabel.setText(String.valueOf((int)(Math.random() * 50) + 20));

                // Closed today (simulated)
                closedTodayLabel.setText(String.valueOf((int)(Math.random() * 15) + 3));
            });
        } catch (Exception e) {
            System.err.println("Error loading KPIs: " + e.getMessage());
        }
    }

    private void loadTicketTrendChart() {
        if (ticketTrendChart == null) return;

        javafx.application.Platform.runLater(() -> {
            ticketTrendChart.getData().clear();

            XYChart.Series<String, Number> openSeries = new XYChart.Series<>();
            openSeries.setName("Opened");

            XYChart.Series<String, Number> closedSeries = new XYChart.Series<>();
            closedSeries.setName("Closed");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

            for (int i = 29; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(formatter);

                // Simulated data - replace with real data
                int opened = (int)(Math.random() * 15) + 5;
                int closed = (int)(Math.random() * 12) + 3;

                openSeries.getData().add(new XYChart.Data<>(dateStr, opened));
                closedSeries.getData().add(new XYChart.Data<>(dateStr, closed));
            }

            ticketTrendChart.getData().addAll(openSeries, closedSeries);
            applyChartStyle(ticketTrendChart);
        });
    }

    private void loadStatusDistributionChart() {
        if (statusDistributionChart == null) return;

        try {
            Map<String, Long> statusCounts = analyticsService.getTicketsByStatus();

            javafx.application.Platform.runLater(() -> {
                statusDistributionChart.getData().clear();

                statusCounts.forEach((status, count) -> {
                    if (count > 0) {
                        PieChart.Data slice = new PieChart.Data(
                                status + " (" + count + ")", count
                        );
                        statusDistributionChart.getData().add(slice);
                    }
                });

                applyPieChartColors(statusDistributionChart);
            });
        } catch (Exception e) {
            System.err.println("Error loading status distribution: " + e.getMessage());
        }
    }

    private void loadPriorityDistributionChart() {
        if (priorityDistributionChart == null) return;

        try {
            Map<String, Long> priorityCounts = analyticsService.getTicketsByPriority();

            javafx.application.Platform.runLater(() -> {
                priorityDistributionChart.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Tickets by Priority");

                String[] priorities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
                for (String priority : priorities) {
                    Long count = priorityCounts.getOrDefault(priority, 0L);
                    XYChart.Data<String, Number> data = new XYChart.Data<>(priority, count);
                    series.getData().add(data);
                }

                priorityDistributionChart.getData().add(series);
                applyChartStyle(priorityDistributionChart);
            });
        } catch (Exception e) {
            System.err.println("Error loading priority distribution: " + e.getMessage());
        }
    }

    private void loadResolutionTimeChart() {
        if (resolutionTimeChart == null) return;

        javafx.application.Platform.runLater(() -> {
            resolutionTimeChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Avg Resolution Time (hours)");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

            for (int i = 13; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(formatter);

                // Simulated data - replace with real data
                double avgTime = 8 + (Math.random() * 8);
                series.getData().add(new XYChart.Data<>(dateStr, avgTime));
            }

            resolutionTimeChart.getData().add(series);
            applyChartStyle(resolutionTimeChart);
        });
    }

    private void loadCategoryPerformanceChart() {
        if (categoryPerformanceChart == null) return;

        try {
            Map<String, Long> categoryCounts = analyticsService.getTicketsByCategory();

            javafx.application.Platform.runLater(() -> {
                categoryPerformanceChart.getData().clear();

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Tickets by Category");

                categoryCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                        .limit(8)
                        .forEach(entry -> {
                            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                        });

                categoryPerformanceChart.getData().add(series);
                applyChartStyle(categoryPerformanceChart);
            });
        } catch (Exception e) {
            System.err.println("Error loading category performance: " + e.getMessage());
        }
    }

    private void loadAgentWorkloadChart() {
        if (agentWorkloadChart == null) return;

        javafx.application.Platform.runLater(() -> {
            agentWorkloadChart.getData().clear();

            // Simulated agent workload data
            String[] agents = {"John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson", "Bob Miller"};

            for (String agent : agents) {
                int workload = (int)(Math.random() * 25) + 5;
                PieChart.Data slice = new PieChart.Data(agent + " (" + workload + ")", workload);
                agentWorkloadChart.getData().add(slice);
            }

            applyPieChartColors(agentWorkloadChart);
        });
    }

    private void loadSLAComplianceChart() {
        if (slaComplianceChart == null) return;

        javafx.application.Platform.runLater(() -> {
            slaComplianceChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("SLA Compliance %");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");

            for (int i = 29; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                String dateStr = date.format(formatter);

                // Simulated compliance data (85-100%)
                double compliance = 85 + (Math.random() * 15);
                series.getData().add(new XYChart.Data<>(dateStr, compliance));
            }

            slaComplianceChart.getData().add(series);
            applyChartStyle(slaComplianceChart);
        });
    }

    private void loadWeeklyActivityChart() {
        if (weeklyActivityChart == null) return;

        javafx.application.Platform.runLater(() -> {
            weeklyActivityChart.getData().clear();

            XYChart.Series<String, Number> createdSeries = new XYChart.Series<>();
            createdSeries.setName("Created");

            XYChart.Series<String, Number> resolvedSeries = new XYChart.Series<>();
            resolvedSeries.setName("Resolved");

            XYChart.Series<String, Number> inProgressSeries = new XYChart.Series<>();
            inProgressSeries.setName("In Progress");

            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

            for (String day : days) {
                createdSeries.getData().add(new XYChart.Data<>(day, (int)(Math.random() * 20) + 10));
                resolvedSeries.getData().add(new XYChart.Data<>(day, (int)(Math.random() * 15) + 5));
                inProgressSeries.getData().add(new XYChart.Data<>(day, (int)(Math.random() * 10) + 3));
            }

            weeklyActivityChart.getData().addAll(createdSeries, resolvedSeries, inProgressSeries);
            applyChartStyle(weeklyActivityChart);
        });
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refreshing statistics...");
        loadAllStatistics();
    }

    @FXML
    private void handleExport() {
        System.out.println("📊 Exporting statistics...");
        showAlert("Export", "Statistics export feature coming soon!");
    }

    @FXML
    private void handleTimeRangeChange() {
        String selected = timeRangeCombo.getValue();
        if ("Custom".equals(selected)) {
            startDatePicker.setDisable(false);
            endDatePicker.setDisable(false);
        } else {
            startDatePicker.setDisable(true);
            endDatePicker.setDisable(true);
            loadAllStatistics();
        }
    }

    private void applyChartStyle(Chart chart) {
        chart.setAnimated(true);
        chart.setLegendVisible(true);
    }

    private void applyPieChartColors(PieChart chart) {
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("OPEN", "#0969da");
        colorMap.put("IN_PROGRESS", "#0969da");
        colorMap.put("RESOLVED", "#1a7f37");
        colorMap.put("CLOSED", "#57606a");

        chart.getData().forEach(data -> {
            String name = data.getName().toUpperCase();
            for (Map.Entry<String, String> entry : colorMap.entrySet()) {
                if (name.contains(entry.getKey())) {
                    data.getNode().setStyle("-fx-pie-color: " + entry.getValue() + ";");
                    break;
                }
            }
        });
    }

    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
        if (refreshButton != null) {
            refreshButton.setDisable(show);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }
}