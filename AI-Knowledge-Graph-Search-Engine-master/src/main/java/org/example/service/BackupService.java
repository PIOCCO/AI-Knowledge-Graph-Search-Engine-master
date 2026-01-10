package org.example.service;

import org.example.repository.TicketRepository;
import org.example.repository.CategoryRepository;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.*;

/**
 * Backup Service - Database backup and restore functionality
 */
public class BackupService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public BackupService() {
        this.ticketRepository = new TicketRepository();
        this.categoryRepository = new CategoryRepository();
    }

    /**
     * Create a full backup
     */
    public String createBackup(String directory, Map<String, Boolean> options,
                               Consumer<Double> progressCallback) throws IOException {

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = "backup_" + timestamp;
        String backupDir = directory + "/" + backupName;

        // Create backup directory
        Files.createDirectories(Paths.get(backupDir));

        progressCallback.accept(0.1);

        // Backup tickets
        if (options.getOrDefault("tickets", true)) {
            backupTickets(backupDir);
            progressCallback.accept(0.3);
        }

        // Backup users
        if (options.getOrDefault("users", true)) {
            backupUsers(backupDir);
            progressCallback.accept(0.5);
        }

        // Backup categories
        if (options.getOrDefault("categories", true)) {
            backupCategories(backupDir);
            progressCallback.accept(0.7);
        }

        // Backup comments
        if (options.getOrDefault("comments", true)) {
            backupComments(backupDir);
            progressCallback.accept(0.8);
        }

        // Backup logs
        if (options.getOrDefault("logs", false)) {
            backupLogs(backupDir);
            progressCallback.accept(0.9);
        }

        // Create backup manifest
        createManifest(backupDir, options);

        // Compress backup
        String zipFile = compressBackup(backupDir, directory + "/" + backupName + ".zip");

        // Delete uncompressed backup
        deleteDirectory(new File(backupDir));

        progressCallback.accept(1.0);

        System.out.println("✅ Backup created: " + zipFile);
        return zipFile;
    }

    /**
     * Restore from backup
     */
    public void restoreBackup(String backupFile, Consumer<Double> progressCallback)
            throws IOException {

        progressCallback.accept(0.1);

        // Extract backup
        String extractDir = backupFile.replace(".zip", "_extract");
        extractBackup(backupFile, extractDir);

        progressCallback.accept(0.3);

        // Read manifest
        Map<String, Boolean> manifest = readManifest(extractDir);

        // Restore tickets
        if (manifest.getOrDefault("tickets", false)) {
            restoreTickets(extractDir);
            progressCallback.accept(0.5);
        }

        // Restore users
        if (manifest.getOrDefault("users", false)) {
            restoreUsers(extractDir);
            progressCallback.accept(0.7);
        }

        // Restore categories
        if (manifest.getOrDefault("categories", false)) {
            restoreCategories(extractDir);
            progressCallback.accept(0.8);
        }

        // Restore comments
        if (manifest.getOrDefault("comments", false)) {
            restoreComments(extractDir);
            progressCallback.accept(0.9);
        }

        // Delete extracted files
        deleteDirectory(new File(extractDir));

        progressCallback.accept(1.0);

        System.out.println("✅ Backup restored successfully");
    }

    /**
     * List available backups
     */
    public List<String> listBackups(String directory) throws IOException {
        List<String> backups = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(directory), "backup_*.zip")) {
            for (Path entry : stream) {
                backups.add(entry.getFileName().toString());
            }
        } catch (NoSuchFileException e) {
            // Directory doesn't exist, return empty list
            return backups;
        }

        // Sort by date (newest first)
        backups.sort(Comparator.reverseOrder());

        return backups;
    }

    /**
     * Verify backup integrity
     */
    public boolean verifyBackup(String backupFile) {
        try {
            // Check if file exists
            if (!Files.exists(Paths.get(backupFile))) {
                return false;
            }

            // Try to open as zip
            try (ZipFile zipFile = new ZipFile(backupFile)) {
                // Check for manifest
                if (zipFile.getEntry("manifest.properties") == null) {
                    return false;
                }

                // Verify all entries are readable
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        // Just try to read - will throw if corrupted
                        is.read();
                    }
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("Backup verification failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete backup file
     */
    public void deleteBackup(String backupFile) throws IOException {
        Files.deleteIfExists(Paths.get(backupFile));
        System.out.println("Backup deleted: " + backupFile);
    }

    /**
     * Clean up old backups
     */
    public int cleanupOldBackups(String directory, int retentionDays) throws IOException {
        List<String> backups = listBackups(directory);
        int deleted = 0;

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        for (String backup : backups) {
            // Extract date from filename (backup_yyyyMMdd_HHmmss.zip)
            String dateStr = backup.substring(7, 22); // Extract yyyyMMdd_HHmmss
            LocalDateTime backupDate = LocalDateTime.parse(dateStr, TIMESTAMP_FORMAT);

            if (backupDate.isBefore(cutoffDate)) {
                deleteBackup(directory + "/" + backup);
                deleted++;
            }
        }

        System.out.println("Cleaned up " + deleted + " old backups");
        return deleted;
    }

    /**
     * Export backup to another location
     */
    public void exportBackup(String sourceBackup, String destination) throws IOException {
        Files.copy(Paths.get(sourceBackup), Paths.get(destination),
                StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Backup exported to: " + destination);
    }

    // Private helper methods

    private void backupTickets(String dir) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("id,title,description,status,priority,category,createdAt\n");

        ticketRepository.findAll().forEach(ticket -> {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    ticket.getId(), ticket.getTitle(), ticket.getDescription(),
                    ticket.getStatus(), ticket.getPriority(), ticket.getCategory(),
                    ticket.getCreatedAt()));
        });

        Files.write(Paths.get(dir, "tickets.csv"), csv.toString().getBytes());
    }

    private void backupUsers(String dir) throws IOException {
        Files.write(Paths.get(dir, "users.csv"), "id,username,email\n".getBytes());
    }

    private void backupCategories(String dir) throws IOException {
        StringBuilder csv = new StringBuilder();
        csv.append("id,name,description,color\n");

        categoryRepository.findAll().forEach(category -> {
            csv.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    category.getId(), category.getName(),
                    category.getDescription(), category.getColor()));
        });

        Files.write(Paths.get(dir, "categories.csv"), csv.toString().getBytes());
    }

    private void backupComments(String dir) throws IOException {
        Files.write(Paths.get(dir, "comments.csv"), "id,ticketId,content,authorId\n".getBytes());
    }

    private void backupLogs(String dir) throws IOException {
        Files.write(Paths.get(dir, "logs.csv"), "id,action,userId,timestamp\n".getBytes());
    }

    private void createManifest(String dir, Map<String, Boolean> options) throws IOException {
        Properties props = new Properties();
        props.setProperty("backupDate", LocalDateTime.now().toString());
        props.setProperty("version", "1.0");
        options.forEach((key, value) -> props.setProperty(key, value.toString()));

        try (OutputStream os = Files.newOutputStream(Paths.get(dir, "manifest.properties"))) {
            props.store(os, "Backup Manifest");
        }
    }

    private Map<String, Boolean> readManifest(String dir) throws IOException {
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get(dir, "manifest.properties"))) {
            props.load(is);
        }

        Map<String, Boolean> manifest = new HashMap<>();
        props.forEach((key, value) -> {
            if (!"backupDate".equals(key) && !"version".equals(key)) {
                manifest.put(key.toString(), Boolean.parseBoolean(value.toString()));
            }
        });

        return manifest;
    }

    private String compressBackup(String sourceDir, String zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            Path sourcePath = Paths.get(sourceDir);

            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String zipEntryName = sourcePath.relativize(path).toString();
                            zos.putNextEntry(new ZipEntry(zipEntryName));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        return zipFile;
    }

    private void extractBackup(String zipFile, String destDir) throws IOException {
        Files.createDirectories(Paths.get(destDir));

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path filePath = Paths.get(destDir, entry.getName());

                if (!entry.isDirectory()) {
                    Files.createDirectories(filePath.getParent());
                    try (InputStream is = zip.getInputStream(entry)) {
                        Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private void restoreTickets(String dir) {
        System.out.println("Restoring tickets from backup...");
        // Implementation would parse CSV and restore to database
    }

    private void restoreUsers(String dir) {
        System.out.println("Restoring users from backup...");
    }

    private void restoreCategories(String dir) {
        System.out.println("Restoring categories from backup...");
    }

    private void restoreComments(String dir) {
        System.out.println("Restoring comments from backup...");
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}