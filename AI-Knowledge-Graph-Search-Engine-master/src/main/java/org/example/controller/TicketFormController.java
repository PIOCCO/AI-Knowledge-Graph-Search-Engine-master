package org.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.model.Ticket;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.example.model.Category;
import org.example.model.User;
import org.example.model.ClassificationResult;
import org.example.repository.CategoryRepository;
import org.example.repository.UserRepository;
import org.example.service.MLClassificationService;

public class TicketFormController implements Initializable {

    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> comboPriority;
    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private ComboBox<String> comboAssignTo;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnAnalyzeAI;
    @FXML
    private Label lblAIResult;
    @FXML
    private ProgressIndicator aiProgress;

    private boolean isEditMode = false;
    private Ticket currentTicket;
    private TicketSaveCallback saveCallback;

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private MLClassificationService mlService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userRepository = new UserRepository();
        categoryRepository = new CategoryRepository();
        mlService = new MLClassificationService();

        setupComboBoxes();
        lblAIResult.setVisible(false);
        aiProgress.setVisible(false);
    }

    private void setupComboBoxes() {
        // Priority options
        comboPriority.setItems(FXCollections.observableArrayList(
                "Low", "Medium", "High", "Critical"));
        comboPriority.setValue("Medium");

        // Category options - Load from DB
        List<Category> categories = categoryRepository.findAll();
        List<String> categoryNames = categories.stream()
                .map(Category::getName)
                .collect(Collectors.toList());

        if (categoryNames.isEmpty()) {
            categoryNames.add("General");
        }

        comboCategory.setItems(FXCollections.observableArrayList(categoryNames));
        if (!categoryNames.isEmpty()) {
            comboCategory.setValue(categoryNames.get(0));
        }

        // Assign to options - Load from DB
        List<User> users = userRepository.findAll();
        List<String> usernames = users.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        usernames.add(0, "Unassigned");

        comboAssignTo.setItems(FXCollections.observableArrayList(usernames));
        comboAssignTo.setValue("Unassigned");
    }

    public void setEditMode(Ticket ticket) {
        this.isEditMode = true;
        this.currentTicket = ticket;

        txtTitle.setText(ticket.getTitle());
        txtDescription.setText(ticket.getDescription());
        comboPriority.setValue(ticket.getPriority());
        comboCategory.setValue(ticket.getCategory());
        comboAssignTo.setValue(ticket.getAssignedTo());

        btnSave.setText("Update Ticket");
    }

    public void setSaveCallback(TicketSaveCallback callback) {
        this.saveCallback = callback;
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        if (!validateForm()) {
            return;
        }

        // Create or update ticket
        Ticket ticket;
        if (isEditMode) {
            ticket = currentTicket;
            ticket.setTitle(txtTitle.getText().trim());
            ticket.setDescription(txtDescription.getText().trim());
            ticket.setPriority(comboPriority.getValue());
            ticket.setCategory(comboCategory.getValue());
            ticket.setAssignedTo(comboAssignTo.getValue());
        } else {
            ticket = new Ticket();
            ticket.setTitle(txtTitle.getText().trim());
            ticket.setDescription(txtDescription.getText().trim());
            ticket.setStatus("Open");
            ticket.setPriority(comboPriority.getValue());
            ticket.setCategory(comboCategory.getValue());
            ticket.setAssignedTo(comboAssignTo.getValue());
            ticket.setCreatedBy("Admin User");
        }

        // Callback to save ticket
        if (saveCallback != null) {
            saveCallback.onSave(ticket, isEditMode);
        }

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Ticket " + (isEditMode ? "updated" : "created") + " successfully!");
        alert.showAndWait();

        // Close window
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    @FXML
    private void handleAnalyzeAI() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();

        if (description.isEmpty()) {
            showAlert("Please enter a description first", Alert.AlertType.WARNING);
            return;
        }

        // Show AI analysis in progress
        aiProgress.setVisible(true);
        btnAnalyzeAI.setDisable(true);
        lblAIResult.setVisible(false);

        // Create temporary ticket for analysis
        Ticket tempTicket = new Ticket();
        tempTicket.setId("TEMP");
        tempTicket.setTitle(title.isEmpty() ? "Untitled" : title);
        tempTicket.setDescription(description);
        tempTicket.setPriority(comboPriority.getValue());

        // Call real ML service
        mlService.classifyTicketAsync(tempTicket)
                .thenAccept(result -> {
                    javafx.application.Platform.runLater(() -> {
                        aiProgress.setVisible(false);
                        btnAnalyzeAI.setDisable(false);

                        if (result != null) {
                            // Update UI with real prediction
                            comboCategory.setValue(result.getCategoryName());

                            String confidenceStr = String.format("%.1f", result.getConfidence() * 100);
                            lblAIResult.setText("🤖 AI Prediction: " + result.getCategoryName() +
                                    " (" + confidenceStr + "% confidence)");
                            lblAIResult.setVisible(true);

                            // Optional: Update priority if the ML model returned one (it doesn't currently,
                            // but could)
                        } else {
                            lblAIResult.setText("⚠️ AI Service unavailable or failed.");
                            lblAIResult.setVisible(true);
                        }
                    });
                });
    }

    private boolean validateForm() {
        if (txtTitle.getText().trim().isEmpty()) {
            showAlert("Please enter a ticket title", Alert.AlertType.WARNING);
            return false;
        }

        if (txtDescription.getText().trim().isEmpty()) {
            showAlert("Please enter a description", Alert.AlertType.WARNING);
            return false;
        }

        if (txtTitle.getText().trim().length() < 5) {
            showAlert("Title must be at least 5 characters", Alert.AlertType.WARNING);
            return false;
        }

        if (txtDescription.getText().trim().length() < 10) {
            showAlert("Description must be at least 10 characters", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private String generateTicketId() {
        // This is no longer needed as repository handles ID generation
        return null;
    }

    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Callback interface
    public interface TicketSaveCallback {
        void onSave(Ticket ticket, boolean isEdit);
    }
}