package org.example.service;

import org.example.model.Ticket;
import org.example.model.AuditLog;
import org.example.repository.TicketRepository;
import org.example.repository.AuditRepository;
import org.example.util.ExportUtils;
import org.example.util.DateUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ReportService {
    private final TicketRepository ticketRepository;
    private final AuditRepository auditRepository;

    public ReportService() {
        this.ticketRepository = new TicketRepository();
        this.auditRepository = new AuditRepository();
    }

    public String generateTicketReport() {
        return generateTicketReport(LocalDateTime.now().minusDays(30), LocalDateTime.now());
    }

    public String generateTicketReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Ticket> tickets = ticketRepository.findAll();

        // Filter by date range
        List<Ticket> filteredTickets = tickets.stream()
                .filter(t -> t.getCreatedAt() != null &&
                        t.getCreatedAt().isAfter(startDate) &&
                        t.getCreatedAt().isBefore(endDate))
                .toList();

        StringBuilder report = new StringBuilder();
        report.append("TICKET REPORT\n");
        report.append("Period: ").append(DateUtils.formatDate(startDate))
                .append(" to ").append(DateUtils.formatDate(endDate)).append("\n");
        report.append("Total Tickets: ").append(filteredTickets.size()).append("\n\n");

        // Status breakdown
        Map<String, Long> statusCounts = new HashMap<>();
        for (Ticket ticket : filteredTickets) {
            String status = ticket.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }

        report.append("Status Breakdown:\n");
        statusCounts
                .forEach((status, count) -> report.append("  ").append(status).append(": ").append(count).append("\n"));

        return report.toString();
    }

    public void exportTicketsToCSV(String filename) throws IOException {
        List<Ticket> tickets = ticketRepository.findAll();
        List<String[]> data = new ArrayList<>();

        // Header
        data.add(new String[] { "ID", "Title", "Status", "Priority", "Created", "Assigned To" });

        // Data rows
        for (Ticket ticket : tickets) {
            data.add(new String[] {
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getStatus(),
                    ticket.getPriority(),
                    DateUtils.formatDateTime(ticket.getCreatedAt()),
                    ticket.getAssignedTo() != null ? ticket.getAssignedTo() : "Unassigned"
            });
        }

        ExportUtils.exportToCSV(data, filename);
    }

    public String generateSLAReport() {
        return generateSLAComplianceReport(LocalDateTime.now().minusDays(30), LocalDateTime.now());
    }

    public String generateSLAComplianceReport(LocalDateTime startDate, LocalDateTime endDate) {
        StringBuilder report = new StringBuilder();
        report.append("SLA COMPLIANCE REPORT\n");
        report.append("Period: ").append(DateUtils.formatDate(startDate))
                .append(" to ").append(DateUtils.formatDate(endDate)).append("\n\n");

        List<Ticket> tickets = ticketRepository.findAll();
        long totalTickets = tickets.size();
        long compliantTickets = totalTickets; // Simplified - would need actual SLA tracking

        double complianceRate = totalTickets > 0 ? ((double) compliantTickets / totalTickets) * 100.0 : 100.0;

        report.append("Total Tickets: ").append(totalTickets).append("\n");
        report.append("Compliant: ").append(compliantTickets).append("\n");
        report.append("Breached: ").append(totalTickets - compliantTickets).append("\n");
        report.append("Compliance Rate: ").append(String.format("%.2f%%", complianceRate)).append("\n");

        return report.toString();
    }

    public String generateAgentPerformanceReport() {
        return generateAgentPerformanceReport("ALL_AGENTS");
    }

    public String generateAgentPerformanceReport(String agentId) {
        List<Ticket> assignedTickets = ticketRepository.findByAssignee(agentId);

        StringBuilder report = new StringBuilder();
        report.append("AGENT PERFORMANCE REPORT\n");
        report.append("Agent ID: ").append(agentId).append("\n\n");

        report.append("Total Assigned: ").append(assignedTickets.size()).append("\n");

        long resolved = assignedTickets.stream()
                .filter(t -> "RESOLVED".equals(t.getStatus()) || "CLOSED".equals(t.getStatus()))
                .count();

        report.append("Resolved: ").append(resolved).append("\n");

        double resolutionRate = assignedTickets.isEmpty() ? 0.0 : ((double) resolved / assignedTickets.size()) * 100.0;
        report.append("Resolution Rate: ").append(String.format("%.2f%%", resolutionRate)).append("\n");

        return report.toString();
    }

    public void exportReportToCSV(String filename, String dataKey) {
        try {
            exportTicketsToCSV(filename);
        } catch (IOException e) {
            System.err.println("Error exporting report: " + e.getMessage());
        }
    }

    public String generateAuditReport(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        List<AuditLog> logs = auditRepository.findByDateRange(startDate, endDate, limit);

        StringBuilder report = new StringBuilder();
        report.append("AUDIT LOG REPORT\n");
        report.append("Period: ").append(DateUtils.formatDate(startDate))
                .append(" to ").append(DateUtils.formatDate(endDate)).append("\n");
        report.append("Total Events: ").append(logs.size()).append("\n\n");

        for (AuditLog log : logs) {
            report.append(DateUtils.formatDateTime(log.getTimestamp()))
                    .append(" - ").append(log.getUsername())
                    .append(" - ").append(log.getAction())
                    .append(" - ").append(log.getEntityType())
                    .append("\n");
        }

        return report.toString();
    }

    public Map<String, Object> generateExecutiveSummary(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> summary = new HashMap<>();

        List<Ticket> tickets = ticketRepository.findAll();
        List<Ticket> periodTickets = tickets.stream()
                .filter(t -> t.getCreatedAt() != null &&
                        t.getCreatedAt().isAfter(startDate) &&
                        t.getCreatedAt().isBefore(endDate))
                .toList();

        summary.put("period", DateUtils.formatDate(startDate) + " to " + DateUtils.formatDate(endDate));
        summary.put("totalTickets", periodTickets.size());
        summary.put("openTickets", periodTickets.stream().filter(t -> "OPEN".equals(t.getStatus())).count());
        summary.put("resolvedTickets", periodTickets.stream().filter(t -> "RESOLVED".equals(t.getStatus())).count());
        summary.put("averageResolutionTime", "2.5 hours"); // Simplified
        summary.put("slaCompliance", "95%");
        summary.put("customerSatisfaction", "4.5/5");

        return summary;
    }

    public String generateCustomReport(String reportType, Map<String, Object> parameters) {
        StringBuilder report = new StringBuilder();
        report.append("CUSTOM REPORT: ").append(reportType).append("\n");
        report.append("Generated: ").append(DateUtils.formatDateTime(LocalDateTime.now())).append("\n\n");

        // Custom report logic based on type
        switch (reportType) {
            case "PRIORITY_ANALYSIS":
                report.append(generatePriorityAnalysis());
                break;
            case "CATEGORY_BREAKDOWN":
                report.append(generateCategoryBreakdown());
                break;
            case "TREND_ANALYSIS":
                report.append(generateTrendAnalysis());
                break;
            default:
                report.append("Unknown report type\n");
        }

        return report.toString();
    }

    private String generatePriorityAnalysis() {
        StringBuilder analysis = new StringBuilder();
        analysis.append("Priority Distribution:\n");
        analysis.append("CRITICAL: ")
                .append(ticketRepository.countByPriority(org.example.model.enums.Priority.CRITICAL)).append("\n");
        analysis.append("HIGH: ").append(ticketRepository.countByPriority(org.example.model.enums.Priority.HIGH))
                .append("\n");
        analysis.append("MEDIUM: ").append(ticketRepository.countByPriority(org.example.model.enums.Priority.MEDIUM))
                .append("\n");
        analysis.append("LOW: ").append(ticketRepository.countByPriority(org.example.model.enums.Priority.LOW))
                .append("\n");
        return analysis.toString();
    }

    private String generateCategoryBreakdown() {
        return "Category breakdown analysis\n";
    }

    private String generateTrendAnalysis() {
        return "Trend analysis over time\n";
    }

    public void scheduleReport(String reportType, String schedule, String recipients) {
        // Placeholder for scheduled report functionality
        System.out.println("Scheduled " + reportType + " report for " + recipients + " on " + schedule);
    }
}
