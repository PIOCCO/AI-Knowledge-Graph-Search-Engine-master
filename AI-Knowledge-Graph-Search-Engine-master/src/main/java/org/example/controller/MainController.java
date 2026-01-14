package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import org.example.model.Ticket;
import org.example.repository.TicketRepository;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Sidebar Navigation Buttons
    @FXML
    private Button btnDashboard;
    private boolean isMyTicketsView = false;
    private final String CURRENT_USER_USERNAME = "admin"; // Hardcoded for demo
    private Button activeSidebarButton;
    private ObservableList<Ticket> ticketList;
    private TicketRepository ticketRepository;
    @FXML
    private Button btnTickets;
    @FXML
    private Button btnNewTicket;
    @FXML
    private Button btnCategories;
    @FXML
    private Button btnUsers;

    // Header Elements
    @FXML
    private Label lblPageTitle;
    @FXML
    private TextField searchField;
    @FXML
    private Label lblUsername;
    @FXML
    private Label lblUserRole;

    // Toolbar
    @FXML
    private HBox toolbarContainer;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<String> priorityFilter;

    // Dashboard Stats
    @FXML
    private Label lblTotalTickets;
    @FXML
    private Label lblOpenTickets;
    @FXML
    private Label lblProgressTickets;
    @FXML
    private Label lblResolvedTickets;

    // Views
    @FXML
    private ScrollPane dashboardView;
    @FXML
    private ScrollPane ticketsView;

    // Labels for counts
    @FXML
    private Label recentTicketsCountLabel;
    @FXML
    private Label allTicketsCountLabel;

    // Recent Tickets Table
    @FXML
    private TableView<Ticket> recentTicketsTable;
    @FXML
    private TableColumn<Ticket, String> colTicketId;
    @FXML
    private TableColumn<Ticket, String> colTitle;
    @FXML
    private TableColumn<Ticket, String> colStatus;
    @FXML
    private TableColumn<Ticket, String> colPriority;
    @FXML
    private TableColumn<Ticket, String> colCategory;
    @FXML
    private TableColumn<Ticket, String> colAssignedTo;
    @FXML
    private TableColumn<Ticket, String> colCreatedAt;

    // All Tickets Table
    @FXML
    private TableView<Ticket> allTicketsTable;
    @FXML
    private TableColumn<Ticket, String> colAllTicketId;
    @FXML
    private TableColumn<Ticket, String> colAllTitle;
    @FXML
    private TableColumn<Ticket, String> colAllStatus;
    @FXML
    private TableColumn<Ticket, String> colAllPriority;
    @FXML
    private TableColumn<Ticket, String> colAllCategory;
    @FXML
    private TableColumn<Ticket, String> colAllAssignedTo;
    @FXML
    private TableColumn<Ticket, String> colAllCreatedAt;
    @FXML
    private TableColumn<Ticket, Void> colAllActions;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ticketRepository = new TicketRepository();

        // Set default active button
        setActiveSidebarButton(btnDashboard);

        // Set user info
        lblUsername.setText("Admin User");
        lblUserRole.setText("Administrator");

        // Setup components
        setupFilters();
        // Don't load immediately here, wait for view selection or load dashboard data
        setupRecentTicketsTable();
        setupAllTicketsTable();

        // Initial data load
        refreshData();

        System.out.println("✅ MainController initialized");
    }

    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.setItems(FXCollections.observableArrayList(
                    "Status: All", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"));
            statusFilter.setValue("Status: All");
            statusFilter.setOnAction(e -> handleSearch());
        }

        if (priorityFilter != null) {
            priorityFilter.setItems(FXCollections.observableArrayList(
                    "Severity: All", "LOW", "MEDIUM", "HIGH", "CRITICAL"));
            priorityFilter.setValue("Severity: All");
            priorityFilter.setOnAction(e -> handleSearch());
        }
    }

    private void refreshData() {
        // 1. Get Filters
        String keyword = searchField.getText();
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();

        // Clean up filter values
        if (status != null && status.startsWith("Status: "))
            status = status.replace("Status: ", "");
        if (priority != null && priority.startsWith("Severity: "))
            priority = priority.replace("Severity: ", "");

        // 2. Determine Scope
        String assignee = isMyTicketsView ? CURRENT_USER_USERNAME : null;

        System.out.println("🔄 Refreshing with: Keyword='" + keyword + "', Status='" + status +
                "', Priority='" + priority + "', Assignee='" + assignee + "'");

        // 3. Fetch Data
        List<Ticket> tickets = ticketRepository.searchTickets(keyword, status, priority, assignee);
        ticketList = FXCollections.observableArrayList(tickets);

        // 4. Update UI
        updateTables();
        updateDashboardStats();
    }

    private void updateTables() {
        if (recentTicketsTable != null) {
            int recentCount = Math.min(10, ticketList.size());
            recentTicketsTable.setItems(FXCollections.observableArrayList(ticketList.subList(0, recentCount)));
            if (recentTicketsCountLabel != null)
                recentTicketsCountLabel.setText(recentCount + " Recent Tickets");
        }

        if (allTicketsTable != null) {
            allTicketsTable.setItems(ticketList);
            if (allTicketsCountLabel != null)
                allTicketsCountLabel.setText(ticketList.size() + " Total Tickets");
        }
    }

    private void setupRecentTicketsTable() {
        // Cell value factories
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAssignedTo.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        colCreatedAt.setCellValueFactory(cellData -> {
            long days = ChronoUnit.DAYS.between(
                    cellData.getValue().getCreatedAt(),
                    LocalDateTime.now());
            return new javafx.beans.property.SimpleStringProperty(days + "d");
        });

        // Apply custom cell styling
        colStatus.setCellFactory(col -> createStatusCell());
        colPriority.setCellFactory(col -> createPriorityCell());
        colCategory.setCellFactory(col -> createCategoryCell());
        colAssignedTo.setCellFactory(col -> createAssignedCell());

        // Initialize with empty list (handled by updateTables)
        recentTicketsTable.setItems(FXCollections.observableArrayList());

        if (recentTicketsCountLabel != null) {
            recentTicketsCountLabel.setText("0 Recent Tickets");
        }

        // Enable table scrolling
        recentTicketsTable.setFixedCellSize(48);
    }

    private void setupAllTicketsTable() {
        // Cell value factories
        colAllTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAllTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAllStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAllPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colAllCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAllAssignedTo.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        colAllCreatedAt.setCellValueFactory(cellData -> {
            long days = ChronoUnit.DAYS.between(
                    cellData.getValue().getCreatedAt(),
                    LocalDateTime.now());
            return new javafx.beans.property.SimpleStringProperty(days + "d");
        });

        // Apply styling
        colAllStatus.setCellFactory(col -> createStatusCell());
        colAllPriority.setCellFactory(col -> createPriorityCell());
        colAllCategory.setCellFactory(col -> createCategoryCell());
        colAllAssignedTo.setCellFactory(col -> createAssignedCell());

        // Actions column
        colAllActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                // Apply Styles
                viewBtn.getStyleClass().addAll("btn-action", "btn-action-view");
                editBtn.getStyleClass().addAll("btn-action", "btn-action-edit");
                deleteBtn.getStyleClass().addAll("btn-action", "btn-action-delete");

                // Add Tooltips
                viewBtn.setTooltip(new Tooltip("View Details"));
                editBtn.setTooltip(new Tooltip("Edit Ticket"));
                deleteBtn.setTooltip(new Tooltip("Delete Ticket"));

                viewBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleViewTicket(ticket);
                });

                editBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleEditTicket(ticket);
                });

                deleteBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleDeleteTicket(ticket);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, viewBtn, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        // Initialize with empty list (handled by updateTables)
        allTicketsTable.setItems(FXCollections.observableArrayList());

        if (allTicketsCountLabel != null) {
            allTicketsCountLabel.setText("0 Total Tickets");
        }

        // Enable table scrolling
        allTicketsTable.setFixedCellSize(48);
    }

    private TableCell<Ticket, String> createStatusCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.getStyleClass().add("status-badge");

                    String normalized = status.toLowerCase().replace(" ", "-");
                    badge.getStyleClass().add("status-" + normalized);

                    setGraphic(badge);
                    setText(null);
                }
            }
        };
    }

    private TableCell<Ticket, String> createPriorityCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    String style = switch (priority.toUpperCase()) {
                        case "CRITICAL" -> "-fx-text-fill: #d1242f; -fx-font-weight: 700;";
                        case "HIGH" -> "-fx-text-fill: #d1242f; -fx-font-weight: 600;";
                        case "MEDIUM" -> "-fx-text-fill: #fb8500; -fx-font-weight: 500;";
                        default -> "-fx-text-fill: #24292e;";
                    };
                    setStyle(style);
                }
            }
        };
    }

    private TableCell<Ticket, String> createCategoryCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String category, boolean empty) {
                super.updateItem(category, empty);
                if (empty || category == null) {
                    setText(null);
                } else {
                    setText(category);
                    setStyle("-fx-text-fill: #0969da;");
                }
            }
        };
    }

    private TableCell<Ticket, String> createAssignedCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String assignedTo, boolean empty) {
                super.updateItem(assignedTo, empty);
                if (empty || assignedTo == null || assignedTo.isEmpty()) {
                    setText("Unassigned");
                    setStyle("-fx-text-fill: #57606a;");
                } else {
                    setText(assignedTo);
                    setStyle("-fx-text-fill: #24292e;");
                }
            }
        };
    }

    private void updateDashboardStats() {
        long total = ticketList.size();
        long open = ticketList.stream()
                .filter(t -> "OPEN".equalsIgnoreCase(t.getStatus()) || "Open".equalsIgnoreCase(t.getStatus()))
                .count();
        long progress = ticketList.stream()
                .filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) ||
                        "In Progress".equalsIgnoreCase(t.getStatus()))
                .count();
        long resolved = ticketList.stream()
                .filter(t -> "RESOLVED".equalsIgnoreCase(t.getStatus()) ||
                        "Resolved".equalsIgnoreCase(t.getStatus()))
                .count();

        lblTotalTickets.setText(String.valueOf(total));
        lblOpenTickets.setText(String.valueOf(open));
        lblProgressTickets.setText(String.valueOf(progress));
        lblResolvedTickets.setText(String.valueOf(resolved));
    }

    // Navigation Handlers
    @FXML
    private void handleDashboard() {
        setActiveSidebarButton(btnDashboard);
        lblPageTitle.setText("Dashboard");
        showDashboard();
    }

    @FXML
    private void handleTickets() {
        setActiveSidebarButton(btnTickets);
        lblPageTitle.setText("My Tickets");
        isMyTicketsView = true;

        // Reset filters for a fresh view or keep them?
        // Let's keep them but refresh data
        refreshData();

        showTicketsView();
    }

    @FXML
    private void handleAllTickets() {
        // Since we don't have a button for this in the sidebar declaration in
        // controller (just generic logic),
        // we might need to highlight one. Assuming the user clicked the button.
        // But activeSidebarButton is not passed here.
        // Usually you'd have @FXML Button btnAllTickets;

        lblPageTitle.setText("All Tickets");
        isMyTicketsView = false;
        refreshData();
        showTicketsView();
    }

    @FXML
    private void handleNewTicket() {
        openTicketForm(null);
    }

    @FXML
    private void handleCategories() {
        setActiveSidebarButton(btnCategories);
        lblPageTitle.setText("Categories");
        openCategoryManagement();
    }

    @FXML
    private void handleUsers() {
        setActiveSidebarButton(btnUsers);
        lblPageTitle.setText("Users");
        openUserManagement();
    }

    @FXML
    private void handleReports() {
        try {
            URL resourceUrl = getClass().getResource("/fxml/ReportsView.fxml");
            if (resourceUrl == null) {
                System.err.println("❌ Critical Error: Could not find resource /fxml/ReportsView.fxml");
                showAlert("Configuration Error",
                        "Could not locate ReportsView.fxml. Please ensure the project is fully rebuilt.",
                        Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 800);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("📊 Reports & Analytics");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.NONE);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open reports: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSettings() {
        showAlert("Settings", "Settings panel coming soon", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.exit(0);
            }
        });
    }

    @FXML
    private void handleSearch() {
        refreshData();
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        showAlert("Refreshed", "Data refreshed successfully", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleOpenStats() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StatsView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("📊 Statistics Dashboard");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleViewTicket(Ticket ticket) {
        showAlert("View Ticket", "Ticket: " + ticket.getId() + "\n" + ticket.getTitle(),
                Alert.AlertType.INFORMATION);
    }

    private void handleEditTicket(Ticket ticket) {
        openTicketForm(ticket);
    }

    private void handleDeleteTicket(Ticket ticket) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Ticket");
        alert.setHeaderText("Delete Ticket " + ticket.getId() + "?");
        alert.setContentText("Are you sure you want to delete this ticket? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ticketRepository.delete(ticket.getId());
                handleRefresh();
            }
        });
    }

    private void showDashboard() {
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        ticketsView.setVisible(false);
        ticketsView.setManaged(false);
        if (toolbarContainer != null) {
            toolbarContainer.setVisible(false);
            toolbarContainer.setManaged(false);
        }
    }

    private void showTicketsView() {
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);
        ticketsView.setVisible(true);
        ticketsView.setManaged(true);
        if (toolbarContainer != null) {
            toolbarContainer.setVisible(true);
            toolbarContainer.setManaged(true);
        }
    }

    private void setActiveSidebarButton(Button button) {
        if (activeSidebarButton != null) {
            activeSidebarButton.getStyleClass().remove("active");
        }
        button.getStyleClass().add("active");
        activeSidebarButton = button;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void openTicketForm(Ticket ticket) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TicketForm.fxml"));
            javafx.scene.Parent root = loader.load();

            TicketFormController controller = loader.getController();
            if (ticket != null) {
                controller.setEditMode(ticket);
            }

            controller.setSaveCallback((savedTicket, isEdit) -> {
                if (isEdit) {
                    ticketRepository.update(savedTicket);
                } else {
                    ticketRepository.create(savedTicket);
                }
                handleRefresh();
            });

            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle(ticket == null ? "Create New Ticket" : "Edit Ticket");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open ticket form", Alert.AlertType.ERROR);
        }
    }

    private void openUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserManagement.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("User Management");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open user management", Alert.AlertType.ERROR);
        }
    }

    private void openCategoryManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryManagement.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Category Management");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open category management", Alert.AlertType.ERROR);
        }
    }
}