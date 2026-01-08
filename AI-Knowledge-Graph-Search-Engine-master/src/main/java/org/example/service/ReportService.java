package org.example.service;

import org.example.model.Report;
import org.example.model.Ticket;
import org.example.repository.TicketRepository;
import org.example.util.SecurityUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report Service - Generate and manage reports
 */
public class ReportService {

    private final TicketRepository ticketRepository;
    private final AnalyticsService analyticsService;
    private final Map<String, Report> savedReports;

    public ReportService() {
        this.ticketRepository = new TicketRepository();
        this.analyticsService = new AnalyticsService();
        this.savedReports = new HashMap<>();
    }

    /**
     * Generate comprehensive report
     */
    public Report generateReport(String type, LocalDateTime startDate, LocalDateTime endDate,
                                 boolean includeCharts, boolean includeSummary,
                                 boolean includeDetails) {
        Report report = new Report();
        report.setId(SecurityUtils.generateId());
        report.setName(type);
        report.setType(type);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setIncludeCharts(includeCharts);
        report.setIncludeSummary(includeSummary);
        report.setIncludeDetails(includeDetails);
        report.setGeneratedBy("Current User");

        Map<String, Object> data = new HashMap<>();

        switch (type) {
            case "Ticket Summary Report":
                data = generateTicketSummary(startDate, endDate);
                break;
            case "Agent Performance Report":
                data = generateAgentPerformance(startDate, endDate);
                break;
            case "SLA Compliance Report":
                data = generateSLACompliance(startDate, endDate);
                break;
            case "Category Analysis Report":
                data = generateCategoryAnalysis(startDate, endDate);
                break;
            case "Resolution Time Report":
                data = generateResolutionTime(startDate, endDate);
                break;
            default:
                data = generateTicketSummary(startDate, endDate);
        }

        report.setData(data);
        report.setPreviewText(generatePreviewText(type, data));

        return report;
    }

    private Map<String, Object> generateTicketSummary(LocalDateTime start, LocalDateTime end) {
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(start) && t.getCreatedAt().isBefore(end))
                .collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("totalTickets", tickets.size());
        data.put("openTickets", tickets.stream().filter(t -> "Open".equals(t.getStatus())).count());
        data.put("resolvedTickets", tickets.stream().filter(t -> "Resolved".equals(t.getStatus())).count());
        data.put("closedTickets", tickets.stream().filter(t -> "Closed".equals(t.getStatus())).count());

        // Priority breakdown
        Map<String, Long> priorityBreakdown = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));
        data.put("priorityBreakdown", priorityBreakdown);

        // Category breakdown
        Map<String, Long> categoryBreakdown = tickets.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(Ticket::getCategory, Collectors.counting()));
        data.put("categoryBreakdown", categoryBreakdown);

        return data;
    }

    private Map<String, Object> generateAgentPerformance(LocalDateTime start, LocalDateTime end) {
        List<Ticket> tickets = ticketRepository.findAll();

        Map<String, Object> data = new HashMap<>();

        // Tickets per agent
        Map<String, Long> ticketsPerAgent = tickets.stream()
                .filter(t -> t.getAssignedTo() != null)
                .collect(Collectors.groupingBy(Ticket::getAssignedTo, Collectors.counting()));
        data.put("ticketsPerAgent", ticketsPerAgent);

        // Resolution rate per agent
        Map<String, Double> resolutionRate = new HashMap<>();
        for (Map.Entry<String, Long> entry : ticketsPerAgent.entrySet()) {
            long resolved = tickets.stream()
                    .filter(t -> entry.getKey().equals(t.getAssignedTo()))
                    .filter(t -> "Resolved".equals(t.getStatus()) || "Closed".equals(t.getStatus()))
                    .count();
            resolutionRate.put(entry.getKey(), (double) resolved / entry.getValue() * 100);
        }
        data.put("resolutionRate", resolutionRate);

        return data;
    }

    private Map<String, Object> generateSLACompliance(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> data = new HashMap<>();

        // Simulated SLA data
        data.put("totalTickets", 150);
        data.put("slaCompliant", 135);
        data.put("slaBreach", 15);
        data.put("complianceRate", 90.0);
        data.put("averageResponseTime", "2.5 hours");
        data.put("averageResolutionTime", "24 hours");

        return data;
    }

    private Map<String, Object> generateCategoryAnalysis(LocalDateTime start, LocalDateTime end) {
        List<Ticket> tickets = ticketRepository.findAll();

        Map<String, Object> data = new HashMap<>();

        Map<String, Long> categoryCount = tickets.stream()
                .filter(t -> t.getCategory() != null)
                .collect(Collectors.groupingBy(Ticket::getCategory, Collectors.counting()));
        data.put("categoryCount", categoryCount);

        return data;
    }

    private Map<String, Object> generateResolutionTime(LocalDateTime start, LocalDateTime end) {
        Map<String, Object> data = new HashMap<>();

        // Simulated resolution time data
        data.put("averageTime", "18.5 hours");
        data.put("medianTime", "12 hours");
        data.put("fastestResolution", "30 minutes");
        data.put("slowestResolution", "96 hours");

        return data;
    }

    private String generatePreviewText(String type, Map<String, Object> data) {
        StringBuilder preview = new StringBuilder();
        preview.append("=".repeat(60)).append("\n");
        preview.append(type.toUpperCase()).append("\n");
        preview.append("=".repeat(60)).append("\n\n");

        preview.append("Generated: ").append(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        preview.append("SUMMARY:\n");
        preview.append("-".repeat(60)).append("\n");

        data.forEach((key, value) -> {
            preview.append(String.format("%-30s: %s\n",
                    key.replaceAll("([A-Z])", " $1").trim(), value));
        });

        preview.append("\n").append("=".repeat(60)).append("\n");

        return preview.toString();
    }

    /**
     * Export report to file
     */
    public String exportReport(Report report, String format, String directory)
            throws Exception {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = report.getName().replaceAll(" ", "_") + "_" + timestamp;

        switch (format.toUpperCase()) {
            case "PDF":
                return exportToPDF(report, directory, filename);
            case "HTML":
                return exportToHTML(report, directory, filename);
            case "CSV":
                return exportToCSV(report, directory, filename);
            case "EXCEL":
                return exportToExcel(report, directory, filename);
            case "JSON":
                return exportToJSON(report, directory, filename);
            default:
                throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private String exportToPDF(Report report, String dir, String filename) {
        // Simulate PDF export
        System.out.println("Exporting to PDF: " + filename);
        return dir + "/" + filename + ".pdf";
    }

    private String exportToHTML(Report report, String dir, String filename) {
        // Simulate HTML export
        System.out.println("Exporting to HTML: " + filename);
        return dir + "/" + filename + ".html";
    }

    private String exportToCSV(Report report, String dir, String filename) {
        // Simulate CSV export
        System.out.println("Exporting to CSV: " + filename);
        return dir + "/" + filename + ".csv";
    }

    private String exportToExcel(Report report, String dir, String filename) {
        // Simulate Excel export
        System.out.println("Exporting to Excel: " + filename);
        return dir + "/" + filename + ".xlsx";
    }

    private String exportToJSON(Report report, String dir, String filename) {
        // Simulate JSON export
        System.out.println("Exporting to JSON: " + filename);
        return dir + "/" + filename + ".json";
    }

    /**
     * Save report template
     */
    public void saveReport(Report report, String name) {
        report.setName(name);
        savedReports.put(name, report);
        System.out.println("Report saved: " + name);
    }

    /**
     * Load saved report
     */
    public Report loadReport(String name) {
        return savedReports.get(name);
    }

    /**
     * Delete saved report
     */
    public void deleteReport(String name) {
        savedReports.remove(name);
        System.out.println("Report deleted: " + name);
    }

    /**
     * Get list of saved report names
     */
    public List<String> getSavedReportNames() {
        return new ArrayList<>(savedReports.keySet());
    }
}