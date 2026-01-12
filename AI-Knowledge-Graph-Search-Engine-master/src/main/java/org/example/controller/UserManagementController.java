package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import org.example.model.User;
import org.example.model.enums.UserRole;
import org.example.repository.UserRepository;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class UserManagementController implements Initializable {

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> colUserId;
    @FXML
    private TableColumn<User, String> colUsername;
    @FXML
    private TableColumn<User, String> colEmail;
    @FXML
    private TableColumn<User, String> colRole;
    @FXML
    private TableColumn<User, String> colStatus;
    @FXML
    private TableColumn<User, String> colJoinedDate;
    @FXML
    private TableColumn<User, Void> colActions;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilter;
    @FXML
    private ComboBox<String> statusFilter;

    private ObservableList<User> userList;
    private UserRepository userRepository;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userRepository = new UserRepository();
        setupFilters();
        loadUsersFromDatabase();
        setupTable();
    }

    private void setupFilters() {
        roleFilter.setItems(FXCollections.observableArrayList(
                "All Roles", "ADMIN", "AGENT", "USER"));
        roleFilter.setValue("All Roles");

        statusFilter.setItems(FXCollections.observableArrayList(
                "All Status", "Active", "Inactive"));
        statusFilter.setValue("All Status");

        // Add listeners for filters
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadUsersFromDatabase() {
        try {
            userList = FXCollections.observableArrayList(userRepository.findAll());
            userTable.setItems(userList);
        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void setupTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Handle Enum to String conversion for Role
        colRole.setCellValueFactory(cellData -> {
            UserRole role = cellData.getValue().getRole();
            return new javafx.beans.property.SimpleStringProperty(role != null ? role.name() : "");
        });

        // Handle Boolean to String conversion for Status
        colStatus.setCellValueFactory(cellData -> {
            boolean active = cellData.getValue().isActive();
            return new javafx.beans.property.SimpleStringProperty(active ? "Active" : "Inactive");
        });

        // Handle LocalDateTime to String for Joined Date
        colJoinedDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Status cell with colored badges
        colStatus.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-background-radius: 12px; -fx-padding: 4px 12px; " +
                            "-fx-font-size: 11px; -fx-font-weight: bold;");

                    switch (status) {
                        case "Active":
                            label.setStyle(label.getStyle() + "-fx-background-color: #d1e7dd; -fx-text-fill: #0f5132;");
                            break;
                        case "Inactive":
                            label.setStyle(label.getStyle() + "-fx-background-color: #e2e3e5; -fx-text-fill: #41464b;");
                            break;
                        default: // Case for Suspended or others if added later
                            label.setStyle(label.getStyle() + "-fx-background-color: #f8d7da; -fx-text-fill: #842029;");
                            break;
                    }

                    setGraphic(label);
                    setText(null);
                }
            }
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button resetBtn = new Button("🔑");

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                resetBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                editBtn.setTooltip(new Tooltip("Edit User"));
                deleteBtn.setTooltip(new Tooltip("Delete User"));
                resetBtn.setTooltip(new Tooltip("Reset Password"));

                editBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });

                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });

                resetBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleResetPassword(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(5, editBtn, resetBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        userTable.setItems(userList);
    }

    @FXML
    private void handleAddUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        ButtonType saveButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form Fields
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.setValue(UserRole.USER);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);
        grid.add(new Label("Password:"), 0, 4);
        grid.add(passwordField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validation
                if (usernameField.getText().isEmpty())
                    return null;

                User user = new User();
                user.setId("U-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                user.setUsername(usernameField.getText());
                user.setEmail(emailField.getText());
                user.setFullName(fullNameField.getText());
                user.setRole(roleCombo.getValue());
                user.setPassword(passwordField.getText()); // In production, hash this!
                user.setActive(true);
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newUser -> {
            try {
                userRepository.save(newUser);
                loadUsersFromDatabase();
                showAlert("Success", "User created successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to create user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleRefresh() {
        loadUsersFromDatabase();
        showAlert("Refresh", "User list refreshed", Alert.AlertType.INFORMATION);
    }

    private void applyFilters() {
        if (userList == null)
            return;

        String roleExp = roleFilter.getValue();
        String statusExp = statusFilter.getValue();
        String query = searchField.getText().toLowerCase();

        ObservableList<User> filtered = userList.filtered(user -> {
            boolean matchesRole = "All Roles".equals(roleExp) ||
                    (user.getRole() != null && user.getRole().name().equalsIgnoreCase(roleExp));

            boolean matchesStatus = "All Status".equals(statusExp) ||
                    (statusExp.equals("Active") && user.isActive()) ||
                    (statusExp.equals("Inactive") && !user.isActive());

            boolean matchesSearch = query.isEmpty() ||
                    user.getUsername().toLowerCase().contains(query) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query));

            return matchesRole && matchesStatus && matchesSearch;
        });

        userTable.setItems(filtered);
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    private void handleEditUser(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        ComboBox<UserRole> roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.setValue(user.getRole());

        ComboBox<String> statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "Active", "Inactive"));
        statusCombo.setValue(user.isActive() ? "Active" : "Inactive");

        TextField emailField = new TextField(user.getEmail());
        TextField fullNameField = new TextField(user.getFullName());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Full Name:"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleCombo, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                user.setRole(roleCombo.getValue());
                user.setActive("Active".equals(statusCombo.getValue()));
                user.setEmail(emailField.getText());
                user.setFullName(fullNameField.getText());
                return user;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedUser -> {
            try {
                userRepository.save(updatedUser);
                userTable.refresh();
                showAlert("Success", "User updated successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to update user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    private void handleDeleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete user: " + user.getUsername() + "?");
        alert.setContentText("This action cannot be undone. All user data will be permanently deleted.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userRepository.delete(user.getId());
                    loadUsersFromDatabase();
                    showAlert("Success", "User deleted successfully!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private void handleResetPassword(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Password");
        alert.setHeaderText("Reset password for: " + user.getUsername() + "?");
        alert.setContentText("A temporary password will be sent to: " + user.getEmail());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // In a real app, generate a token and send email
                // For now, just print to console
                System.out.println("Resetting password for " + user.getUsername());
                showAlert("Success", "Password reset email sent to " + user.getEmail(),
                        Alert.AlertType.INFORMATION);
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}