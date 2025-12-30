package org.example.util;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class ExportUtils {

    public static void exportToCSV(List<String[]> data, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            for (String[] row : data) {
                writer.append(String.join(",", row));
                writer.append("\n");
            }
        }
    }

    public static String generateCSVRow(String... values) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                row.append(",");
            row.append(escapeCSV(values[i]));
        }
        return row.toString();
    }

    private static String escapeCSV(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static String generateFilename(String prefix, String extension) {
        String timestamp = LocalDateTime.now().toString().replaceAll("[:\\-.]", "");
        return prefix + "_" + timestamp + "." + extension;
    }

    public static void exportToJSON(String jsonContent, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(jsonContent);
        }
    }

    public static String formatForExport(Object value) {
        if (value == null)
            return "";
        if (value instanceof LocalDateTime) {
            return DateUtils.formatDateTime((LocalDateTime) value);
        }
        return value.toString();
    }

    public static byte[] generatePDFReport(String title, List<String[]> data) {
        // Placeholder for PDF generation
        // In a real implementation, you would use a library like iText or Apache PDFBox
        return new byte[0];
    }

    public static String generateHTMLReport(String title, List<String[]> headers, List<List<String>> data) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<title>").append(title).append("</title>");
        html.append("<style>table{border-collapse:collapse;width:100%;}");
        html.append("th,td{border:1px solid #ddd;padding:8px;text-align:left;}");
        html.append("th{background-color:#4CAF50;color:white;}</style>");
        html.append("</head><body>");
        html.append("<h1>").append(title).append("</h1>");
        html.append("<table><thead><tr>");

        for (String[] header : headers) {
            for (String h : header) {
                html.append("<th>").append(h).append("</th>");
            }
        }

        html.append("</tr></thead><tbody>");

        for (List<String> row : data) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td>").append(cell).append("</td>");
            }
            html.append("</tr>");
        }

        html.append("</tbody></table></body></html>");
        return html.toString();
    }
}
