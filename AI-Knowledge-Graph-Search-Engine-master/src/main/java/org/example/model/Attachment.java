package org.example.model;

import java.time.LocalDateTime;

public class Attachment {
    private String id;
    private String filename;
    private String originalFilename;
    private String filePath;
    private String fileUrl;
    private String mimeType;
    private long fileSize;
    private String ticketId;
    private String commentId;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
    private String checksum;

    public Attachment() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Attachment(String id, String filename, String filePath, long fileSize) {
        this();
        this.id = id;
        this.filename = filename;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFileSizeFormatted() {
        if (fileSize < 1024)
            return fileSize + " B";
        if (fileSize < 1024 * 1024)
            return String.format("%.2f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024)
            return String.format("%.2f MB", fileSize / (1024.0 * 1024));
        return String.format("%.2f GB", fileSize / (1024.0 * 1024 * 1024));
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id='" + id + '\'' +
                ", filename='" + filename + '\'' +
                ", fileSize=" + getFileSizeFormatted() +
                '}';
    }
}
