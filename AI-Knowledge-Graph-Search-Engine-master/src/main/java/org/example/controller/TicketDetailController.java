package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;

import org.example.model.Ticket;
import org.example.model.Comment;
import org.example.repository.TicketRepository;
import org.example.repository.CommentRepository;
import org.example.service.NotificationManager;
import org.example.service.AIService;
import org.example.util.SecurityUtils;
import org.example.util.DateUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Comprehensive Ticket Detail View with comments, history, and AI suggestions
 */
public class TicketDetailController implements Initializable {

    @FXML private Label ticketIdLabel;
    @FXML private Label ticketTitleLabel;
    @FXML private Label statusLabel;
    @FXML private Label priorityLabel;
    @FXML private Label categoryLabel;
    @FXML private Label assignedToLabel;
    @FXML private Label createdByLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label updatedAtLabel;
    @FXML private TextArea descriptionArea;

    @FXML private VBox commentsContainer;
    @FXML private TextArea newCommentArea;
    @FXML private Button addCommentButton;

    @FXML private VBox historyContainer;
    @FXML private VBox suggestionsContainer;

    @FXML private ComboBox<String> statusCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private ComboBox<String> assignCombo;

    @FXML private ProgressIndicator loadingIndicator;

    private Ticket currentTicket;
    private TicketRepository ticketRepository;
    private CommentRepository commentRepository;
    private AIService aiService;
    private NotificationManager notificationManager;
    private ObservableList<Comment> comments;

    public TicketDetailController() {
        this.ticketRepository = new TicketRepository();
        this.commentRepository = new CommentRepository();
        this.aiService = new AIService();
        this.notificationManager = NotificationManager.getInstance();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupComboBoxes();
        setupCommentsSection();
    }

    private void setupComboBoxes() {
        statusCombo.setItems(FXCollections.observableArrayList(
                "Open", "In Progress", "Resolved", "Closed"
        ));

        priorityCombo.setItems(FXCollections.observableArrayList(
                "Low", "Medium", "High", "Critical"
        ));

        assignCombo.setItems(FXCollections.observableArrayList(
                "Unassigned", "John Doe", "Jane Smith", "Mike Johnson"
        ));
    }

    private void setupCommentsSection() {
        comments = FXCollections.observableArrayList();
        newCommentArea.setPromptText("Add a comment...");
    }

    public void setTicket(Ticket ticket) {
        this.currentTicket = ticket;
        loadTicketDetails();
        loadComments();
        loadHistory();
        loadAISuggestions();
    }

    private void loadTicketDetails() {
        if (currentTicket == null) return;

        ticketIdLabel.setText(currentTicket.getId());
        ticketTitleLabel.setText(currentTicket.getTitle());
        descriptionArea.setText(currentTicket.getDescription());

        // Status badge
        statusLabel.setText(currentTicket.getStatus());
        applyStatusStyle(statusLabel, currentTicket.getStatus());

        // Priority
        priorityLabel.setText(currentTicket.getPriority());
        applyPriorityStyle(priorityLabel, currentTicket.getPriority());

        categoryLabel.setText(currentTicket.getCategory() != null ?
                currentTicket.getCategory() : "Uncategorized");

        assignedToLabel.setText(currentTicket.getAssignedTo() != null ?
                currentTicket.getAssignedTo() : "Unassigned");

        createdByLabel.setText(currentTicket.getCreatedBy() != null ?
                currentTicket.getCreatedBy() : "Unknown");

        createdAtLabel.setText(DateUtils.formatDateTime(currentTicket.getCreatedAt()));
        updatedAtLabel.setText(DateUtils.formatDateTime(currentTicket.getUpdatedAt()));

        // Set combo box values
        statusCombo.setValue(currentTicket.getStatus());
        priorityCombo.setValue(currentTicket.getPriority());
        assignCombo.setValue(currentTicket.getAssignedTo() != null ?
                currentTicket.getAssignedTo() : "Unassigned");
    }

    private void loadComments() {
        if (currentTicket == null) return;

        commentsContainer.getChildren().clear();
        List<Comment> ticketComments = commentRepository.findByTicketId(currentTicket.getId());

        if (ticketComments.isEmpty()) {
            Label noComments = new Label("No comments yet. Be the first to comment!");
            noComments.setStyle("-fx-text-fill: #57606a; -fx-padding: 20px;");
            commentsContainer.getChildren().add(noComments);
        } else {
            for (Comment comment : ticketComments) {
                commentsContainer.getChildren().add(createCommentCard(comment));
            }
        }
    }

    private VBox createCommentCard(Comment comment) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #d0d7de; " +
                "-fx-border-width: 1; -fx-border-radius: 6; " +
                "-fx-background-radius: 6; -fx-padding: 15;");
        card.setPadding(new Insets(15));

        // Header
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label authorLabel = new Label(comment.getAuthorName() != null ?
                comment.getAuthorName() : "Unknown User");
        authorLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: #24292e;");

        Label timeLabel = new Label(DateUtils.getTimeAgo(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #57606a; -fx-font-size: 12px;");

        header.getChildren().addAll(authorLabel, timeLabel);

        if (comment.isEdited()) {
            Label editedLabel = new Label("(edited)");
            editedLabel.setStyle("-fx-text-fill: #57606a; -fx-font-size: 11px; -fx-font-style: italic;");
            header.getChildren().add(editedLabel);
        }

        // Content
        TextArea contentArea = new TextArea(comment.getContent());
        contentArea.setWrapText(true);
        contentArea.setEditable(false);
        contentArea.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
        contentArea.setPrefRowCount(3);

        card.getChildren().addAll(header, contentArea);
        return card;
    }

    private void loadHistory() {
        if (historyContainer == null) return;

        historyContainer.getChildren().clear();

        // Simulated history - in real app, fetch from audit log
        addHistoryItem("Created", currentTicket.getCreatedBy(),
                currentTicket.getCreatedAt());

        if (currentTicket.getAssignedTo() != null) {
            addHistoryItem("Assigned to " + currentTicket.getAssignedTo(),
                    "System", currentTicket.getUpdatedAt());
        }
    }

    private void addHistoryItem(String action, String user, LocalDateTime timestamp) {
        HBox item = new HBox(10);
        item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 8px 0;");

        Label actionLabel = new Label("• " + action);
        actionLabel.setStyle("-fx-text-fill: #24292e;");

        Label userLabel = new Label("by " + user);
        userLabel.setStyle("-fx-text-fill: #57606a; -fx-font-size: 12px;");

        Label timeLabel = new Label(DateUtils.getTimeAgo(timestamp));
        timeLabel.setStyle("-fx-text-fill: #57606a; -fx-font-size: 11px;");

        item.getChildren().addAll(actionLabel, userLabel, timeLabel);
        historyContainer.getChildren().add(item);
    }

    private void loadAISuggestions() {
        if (suggestionsContainer == null || currentTicket == null) return;

        suggestionsContainer.getChildren().clear();

        // AI-powered suggestions
        new Thread(() -> {
            try {
                // Analyze ticket
                var analysis = aiService.analyzeTicket(currentTicket);

                javafx.application.Platform.runLater(() -> {
                    // Suggested category
                    if (analysis.containsKey("classification")) {
                        addSuggestion("💡 Suggested Category",
                                (String) analysis.get("classification"),
                                "Based on AI analysis");
                    }

                    // Suggested priority
                    if (analysis.containsKey("suggestedPriority")) {
                        addSuggestion("⚡ Suggested Priority",
                                analysis.get("suggestedPriority").toString(),
                                "Based on urgency indicators");
                    }

                    // Keywords
                    if (analysis.containsKey("keywords")) {
                        List<String> keywords = (List<String>) analysis.get("keywords");
                        addSuggestion("🔑 Key Topics",
                                String.join(", ", keywords.subList(0, Math.min(5, keywords.size()))),
                                "Extracted from description");
                    }
                });
            } catch (Exception e) {
                System.err.println("Error loading AI suggestions: " + e.getMessage());
            }
        }).start();
    }

    private void addSuggestion(String title, String content, String subtitle) {
        VBox suggestion = new VBox(5);
        suggestion.setStyle("-fx-background-color: #f6f8fa; -fx-padding: 12px; " +
                "-fx-border-radius: 4; -fx-background-radius: 4;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 13px;");

        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: #0969da; -fx-font-size: 14px;");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setStyle("-fx-text-fill: #57606a; -fx-font-size: 11px;");

        suggestion.getChildren().addAll(titleLabel, contentLabel, subtitleLabel);
        suggestionsContainer.getChildren().add(suggestion);
    }

    @FXML
    private void handleAddComment() {
        String commentText = newCommentArea.getText().trim();
        if (commentText.isEmpty()) {
            showAlert("Empty Comment", "Please enter a comment.", Alert.AlertType.WARNING);
            return;
        }

        Comment comment = new Comment();
        comment.setId(SecurityUtils.generateId());
        comment.setTicketId(currentTicket.getId());
        comment.setContent(commentText);
        comment.setAuthorId("current-user-id"); // Get from session
        comment.setAuthorName("Current User");
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);

        // Notify ticket owner
        if (currentTicket.getCreatedBy() != null) {
            notificationManager.notifyCommentAdded(
                    currentTicket.getId(),
                    "current-user-id",
                    currentTicket.getCreatedBy()
            );
        }

        newCommentArea.clear();
        loadComments();

        showAlert("Success", "Comment added successfully!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleUpdateStatus() {
        String newStatus = statusCombo.getValue();
        if (newStatus != null && !newStatus.equals(currentTicket.getStatus())) {
            currentTicket.setStatus(newStatus);
            currentTicket.setUpdatedAt(LocalDateTime.now());

            if ("Resolved".equals(newStatus) || "Closed".equals(newStatus)) {
                currentTicket.setResolvedAt(LocalDateTime.now());
            }

            ticketRepository.update(currentTicket);

            // Notify relevant parties
            if (currentTicket.getCreatedBy() != null) {
                notificationManager.notifyTicketUpdated(currentTicket, currentTicket.getCreatedBy());
            }

            loadTicketDetails();
            loadHistory();
            showAlert("Success", "Status updated successfully!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleUpdatePriority() {
        String newPriority = priorityCombo.getValue();
        if (newPriority != null && !newPriority.equals(currentTicket.getPriority())) {
            currentTicket.setPriority(newPriority);
            currentTicket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.update(currentTicket);
            loadTicketDetails();
            showAlert("Success", "Priority updated successfully!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleUpdateAssignment() {
        String newAssignee = assignCombo.getValue();
        if ("Unassigned".equals(newAssignee)) {
            newAssignee = null;
        }

        if ((newAssignee == null && currentTicket.getAssignedTo() != null) ||
                (newAssignee != null && !newAssignee.equals(currentTicket.getAssignedTo()))) {

            currentTicket.setAssignedTo(newAssignee);
            currentTicket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.update(currentTicket);

            // Notify new assignee
            if (newAssignee != null) {
                notificationManager.notifyTicketAssigned(currentTicket, newAssignee);
            }

            loadTicketDetails();
            showAlert("Success", "Assignment updated successfully!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleClose() {
        // Close the detail window
        ((javafx.stage.Stage) ticketIdLabel.getScene().getWindow()).close();
    }

    private void applyStatusStyle(Label label, String status) {
        label.getStyleClass().clear();
        label.getStyleClass().add("status-badge");

        switch (status.toUpperCase().replace(" ", "_")) {
            case "OPEN":
                label.getStyleClass().add("status-open");
                break;
            case "IN_PROGRESS":
                label.getStyleClass().add("status-progress");
                break;
            case "RESOLVED":
                label.getStyleClass().add("status-resolved");
                break;
            case "CLOSED":
                label.getStyleClass().add("status-closed");
                break;
        }
    }

    private void applyPriorityStyle(Label label, String priority) {
        String style;
        switch (priority.toUpperCase()) {
            case "CRITICAL":
                style = "-fx-text-fill: #d1242f; -fx-font-weight: 700;";
                break;
            case "HIGH":
                style = "-fx-text-fill: #d1242f; -fx-font-weight: 600;";
                break;
            case "MEDIUM":
                style = "-fx-text-fill: #fb8500; -fx-font-weight: 500;";
                break;
            default:
                style = "-fx-text-fill: #24292e;";
        }
        label.setStyle(style);
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}