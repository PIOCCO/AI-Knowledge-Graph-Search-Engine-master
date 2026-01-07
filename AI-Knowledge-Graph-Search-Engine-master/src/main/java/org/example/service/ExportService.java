package org.example.service;

import org.example.model.Ticket;
import org.example.util.DateUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Advanced Export Service supporting multiple formats
 */
public class ExportService {

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Export tickets to CSV format
     */
    public String exportToCSV(List<Ticket> tickets, String directory) throws IOException {
        String filename = generateFilename("tickets", "csv");
        Path filePath = Paths.get(directory, filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write CSV header
            writer.write("ID,Title,Status,Priority,Category,Assigned To,Created By,Created At,Description\n");

            // Write data rows
            for (Ticket ticket : tickets) {
                writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCSV(ticket.getId()),
                        escapeCSV(ticket.getTitle()),
                        escapeCSV(ticket.getStatus()),
                        escapeCSV(ticket.getPriority()),
                        escapeCSV(ticket.getCategory()),
                        escapeCSV(ticket.getAssignedTo()),
                        escapeCSV(ticket.getCreatedBy()),
                        DateUtils.formatDateTime(ticket.getCreatedAt()),
                        escapeCSV(truncate(ticket.getDescription(), 200))
                ));
            }
        }

        System.out.println("✅ Exported " + tickets.size() + " tickets to " + filePath);
        return filePath.toString();
    }

    /**
     * Export tickets to JSON format
     */
    public String exportToJSON(List<Ticket> tickets, String directory) throws IOException {
        String filename = generateFilename("tickets", "json");
        Path filePath = Paths.get(directory, filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("{\n");
            writer.write("  \"exportDate\": \"" + LocalDateTime.now().toString() + "\",\n");
            writer.write("  \"count\": " + tickets.size() + ",\n");
            writer.write("  \"tickets\": [\n");

            for (int i = 0; i < tickets.size(); i++) {
                Ticket ticket = tickets.get(i);
                writer.write("    {\n");
                writer.write("      \"id\": \"" + escapeJSON(ticket.getId()) + "\",\n");
                writer.write("      \"title\": \"" + escapeJSON(ticket.getTitle()) + "\",\n");
                writer.write("      \"status\": \"" + escapeJSON(ticket.getStatus()) + "\",\n");
                writer.write("      \"priority\": \"" + escapeJSON(ticket.getPriority()) + "\",\n");
                writer.write("      \"category\": \"" + escapeJSON(ticket.getCategory()) + "\",\n");
                writer.write("      \"assignedTo\": \"" + escapeJSON(ticket.getAssignedTo()) + "\",\n");
                writer.write("      \"createdBy\": \"" + escapeJSON(ticket.getCreatedBy()) + "\",\n");
                writer.write("      \"createdAt\": \"" + ticket.getCreatedAt() + "\",\n");
                writer.write("      \"description\": \"" + escapeJSON(ticket.getDescription()) + "\"\n");
                writer.write("    }" + (i < tickets.size() - 1 ? "," : "") + "\n");
            }

            writer.write("  ]\n");
            writer.write("}\n");
        }

        System.out.println("✅ Exported " + tickets.size() + " tickets to " + filePath);
        return filePath.toString();
    }

    /**
     * Export tickets to HTML report format
     */
    public String exportToHTML(List<Ticket> tickets, String directory) throws IOException {
        String filename = generateFilename("tickets_report", "html");
        Path filePath = Paths.get(directory, filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(generateHTMLHeader());
            writer.write("<body>\n");
            writer.write("<div class='container'>\n");
            writer.write("<h1>Ticket Report</h1>\n");
            writer.write("<div class='meta'>Generated: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
                    "</div>\n");
            writer.write("<div class='meta'>Total Tickets: " + tickets.size() + "</div>\n");

            // Summary statistics
            writer.write(generateStatistics(tickets));

            // Tickets table
            writer.write("<h2>Tickets</h2>\n");
            writer.write("<table>\n");
            writer.write("<thead>\n");
            writer.write("<tr><th>ID</th><th>Title</th><th>Status</th><th>Priority</th>" +
                    "<th>Category</th><th>Assigned To</th><th>Created</th></tr>\n");
            writer.write("</thead>\n");
            writer.write("<tbody>\n");

            for (Ticket ticket : tickets) {
                writer.write("<tr>");
                writer.write("<td>" + escapeHTML(ticket.getId()) + "</td>");
                writer.write("<td>" + escapeHTML(ticket.getTitle()) + "</td>");
                writer.write("<td><span class='badge status-" +
                        ticket.getStatus().toLowerCase().replace(" ", "-") + "'>" +
                        escapeHTML(ticket.getStatus()) + "</span></td>");
                writer.write("<td><span class='priority-" +
                        ticket.getPriority().toLowerCase() + "'>" +
                        escapeHTML(ticket.getPriority()) + "</span></td>");
                writer.write("<td>" + escapeHTML(ticket.getCategory()) + "</td>");
                writer.write("<td>" + escapeHTML(ticket.getAssignedTo()) + "</td>");
                writer.write("<td>" + DateUtils.formatDateTime(ticket.getCreatedAt()) + "</td>");
                writer.write("</tr>\n");
            }

            writer.write("</tbody>\n");
            writer.write("</table>\n");
            writer.write("</div>\n");
            writer.write("</body>\n</html>");
        }

        System.out.println("✅ Exported " + tickets.size() + " tickets to " + filePath);
        return filePath.toString();
    }

    /**
     * Export tickets to Excel-compatible format (TSV)
     */
    public String exportToExcel(List<Ticket> tickets, String directory) throws IOException {
        String filename = generateFilename("tickets", "xlsx.tsv");
        Path filePath = Paths.get(directory, filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write TSV header
            writer.write("ID\tTitle\tStatus\tPriority\tCategory\tAssigned To\tCreated By\tCreated At\tDescription\n");

            // Write data rows
            for (Ticket ticket : tickets) {
                writer.write(String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                        escapeTSV(ticket.getId()),
                        escapeTSV(ticket.getTitle()),
                        escapeTSV(ticket.getStatus()),
                        escapeTSV(ticket.getPriority()),
                        escapeTSV(ticket.getCategory()),
                        escapeTSV(ticket.getAssignedTo()),
                        escapeTSV(ticket.getCreatedBy()),
                        DateUtils.formatDateTime(ticket.getCreatedAt()),
                        escapeTSV(truncate(ticket.getDescription(), 500))
                ));
            }
        }

        System.out.println("✅ Exported " + tickets.size() + " tickets to " + filePath);
        return filePath.toString();
    }

    /**
     * Export summary statistics
     */
    public String exportSummary(List<Ticket> tickets, String directory) throws IOException {
        String filename = generateFilename("summary", "txt");
        Path filePath = Paths.get(directory, filename);

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("TICKET SUMMARY REPORT\n");
            writer.write("=====================\n\n");
            writer.write("Generated: " + LocalDateTime.now() + "\n");
            writer.write("Total Tickets: " + tickets.size() + "\n\n");

            // Status breakdown
            writer.write("STATUS BREAKDOWN:\n");
            var statusCounts = countByStatus(tickets);
            statusCounts.forEach((status, count) -> {
                try {
                    writer.write(String.format("  %-15s: %d\n", status, count));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer.write("\nPRIORITY BREAKDOWN:\n");
            var priorityCounts = countByPriority(tickets);
            priorityCounts.forEach((priority, count) -> {
                try {
                    writer.write(String.format("  %-15s: %d\n", priority, count));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            writer.write("\nCATEGORY BREAKDOWN:\n");
            var categoryCounts = countByCategory(tickets);
            categoryCounts.forEach((category, count) -> {
                try {
                    writer.write(String.format("  %-15s: %d\n", category, count));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        System.out.println("✅ Exported summary to " + filePath);
        return filePath.toString();
    }

    // Helper Methods

    private String generateFilename(String prefix, String extension) {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        return prefix + "_" + timestamp + "." + extension;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String escapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeHTML(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeTSV(String value) {
        if (value == null) return "";
        return value.replace("\t", " ")
                .replace("\n", " ")
                .replace("\r", "");
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() > maxLength ? value.substring(0, maxLength) + "..." : value;
    }

    private String generateHTMLHeader() {
        return "<!DOCTYPE html>\n<html>\n<head>\n" +
                "<meta charset='UTF-8'>\n" +
                "<title>Ticket Report</title>\n" +
                "<style>\n" +
                "body { font-family: Arial, sans-serif; margin: 20px; background: #f6f8fa; }\n" +
                ".container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }\n" +
                "h1 { color: #24292e; border-bottom: 2px solid #0969da; padding-bottom: 10px; }\n" +
                "h2 { color: #24292e; margin-top: 30px; }\n" +
                ".meta { color: #57606a; margin: 5px 0; }\n" +
                ".stats { display: flex; gap: 20px; margin: 20px 0; }\n" +
                ".stat-card { background: #f6f8fa; padding: 15px; border-radius: 6px; flex: 1; }\n" +
                ".stat-value { font-size: 32px; font-weight: bold; color: #0969da; }\n" +
                ".stat-label { color: #57606a; font-size: 14px; }\n" +
                "table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n" +
                "th, td { padding: 12px; text-align: left; border-bottom: 1px solid #d0d7de; }\n" +
                "th { background: #f6f8fa; font-weight: 600; color: #24292e; }\n" +
                "tr:hover { background: #f6f8fa; }\n" +
                ".badge { padding: 4px 10px; border-radius: 3px; font-size: 12px; font-weight: 600; }\n" +
                ".status-open { background: #cfe2ff; color: #084298; }\n" +
                ".status-in-progress { background: #cfe2ff; color: #084298; }\n" +
                ".status-resolved { background: #d1e7dd; color: #0f5132; }\n" +
                ".status-closed { background: #e2e3e5; color: #41464b; }\n" +
                ".priority-critical { color: #d1242f; font-weight: 700; }\n" +
                ".priority-high { color: #d1242f; font-weight: 600; }\n" +
                ".priority-medium { color: #fb8500; }\n" +
                "</style>\n</head>\n";
    }

    private String generateStatistics(List<Ticket> tickets) {
        StringBuilder stats = new StringBuilder();
        stats.append("<div class='stats'>\n");

        // Total tickets
        stats.append("<div class='stat-card'><div class='stat-value'>" + tickets.size() +
                "</div><div class='stat-label'>Total Tickets</div></div>\n");

        // Open tickets
        long openCount = tickets.stream().filter(t -> "Open".equalsIgnoreCase(t.getStatus())).count();
        stats.append("<div class='stat-card'><div class='stat-value'>" + openCount +
                "</div><div class='stat-label'>Open</div></div>\n");

        // Resolved tickets
        long resolvedCount = tickets.stream().filter(t -> "Resolved".equalsIgnoreCase(t.getStatus())).count();
        stats.append("<div class='stat-card'><div class='stat-value'>" + resolvedCount +
                "</div><div class='stat-label'>Resolved</div></div>\n");

        stats.append("</div>\n");
        return stats.toString();
    }

    private java.util.Map<String, Long> countByStatus(List<Ticket> tickets) {
        return tickets.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Ticket::getStatus,
                        java.util.stream.Collectors.counting()
                ));
    }

    private java.util.Map<String, Long> countByPriority(List<Ticket> tickets) {
        return tickets.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        Ticket::getPriority,
                        java.util.stream.Collectors.counting()
                ));
    }

    private java.util.Map<String, Long> countByCategory(List<Ticket> tickets) {
        return tickets.stream()
                .filter(t -> t.getCategory() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        Ticket::getCategory,
                        java.util.stream.Collectors.counting()
                ));
    }
}