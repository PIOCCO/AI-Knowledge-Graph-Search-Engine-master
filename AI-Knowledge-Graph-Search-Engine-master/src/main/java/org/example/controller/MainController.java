package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;      // or VBox, StackPane, etc.
import javafx.stage.Stage;
import javafx.scene.Parent;




import org.example.model.Ticket;
import org.example.repository.TicketRepository;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Sidebar Navigation Buttons
    @FXML private Button btnDashboard;
    @FXML private Button btnTickets;
    @FXML private Button btnNewTicket;
    @FXML private Button btnCategories;
    @FXML private Button btnUsers;

    // Header Elements
    @FXML private Label lblPageTitle;
    @FXML private TextField searchField;
    @FXML private Label lblUsername;
    @FXML private Label lblUserRole;

    // Toolbar
    @FXML private HBox toolbarContainer;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;

    // Dashboard Stats
    @FXML private Label lblTotalTickets;
    @FXML private Label lblOpenTickets;
    @FXML private Label lblProgressTickets;
    @FXML private Label lblResolvedTickets;

    // Views
    @FXML private ScrollPane dashboardView;
    @FXML private ScrollPane ticketsView;

    // Recent Tickets Table
    @FXML private TableView<Ticket> recentTicketsTable;
    @FXML private TableColumn<Ticket, String> colTicketId;
    @FXML private TableColumn<Ticket, String> colTitle;
    @FXML private TableColumn<Ticket, String> colStatus;
    @FXML private TableColumn<Ticket, String> colPriority;
    @FXML private TableColumn<Ticket, String> colCategory;
    @FXML private TableColumn<Ticket, String> colAssignedTo;
    @FXML private TableColumn<Ticket, String> colCreatedAt;

    // All Tickets Table
    @FXML private TableView<Ticket> allTicketsTable;
    @FXML private TableColumn<Ticket, String> colAllTicketId;
    @FXML private TableColumn<Ticket, String> colAllTitle;
    @FXML private TableColumn<Ticket, String> colAllStatus;
    @FXML private TableColumn<Ticket, String> colAllPriority;
    @FXML private TableColumn<Ticket, String> colAllCategory;
    @FXML private TableColumn<Ticket, String> colAllAssignedTo;
    @FXML private TableColumn<Ticket, String> colAllCreatedAt;
    @FXML private TableColumn<Ticket, Void> colAllActions;

    private Button activeSidebarButton;
    private ObservableList<Ticket> ticketList;
    private TicketRepository ticketRepository;

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
        loadTicketsFromDatabase();
        setupRecentTicketsTable();
        setupAllTicketsTable();
        updateDashboardStats();

        // Show dashboard by default
        showDashboard();
    }

    private void setupFilters() {
        statusFilter.setItems(FXCollections.observableArrayList(
                "Status: All", "New", "Open", "In Progress", "Resolved", "Closed"
        ));
        statusFilter.setValue("Status: All");

        priorityFilter.setItems(FXCollections.observableArrayList(
                "Severity: All", "Low", "Normal", "High", "Critical"
        ));
        priorityFilter.setValue("Severity: All");
    }

    private void loadTicketsFromDatabase() {
        try {
            List<Ticket> tickets = ticketRepository.findAll();
            ticketList = FXCollections.observableArrayList(tickets);
            System.out.println("✅ Loaded " + tickets.size() + " tickets from Neo4j");
        } catch (Exception e) {
            System.err.println("❌ Error loading tickets: " + e.getMessage());
            ticketList = FXCollections.observableArrayList();
        }
    }

    private void setupRecentTicketsTable() {
        colTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAssignedTo.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        colCreatedAt.setCellValueFactory(cellData -> {
            long days = ChronoUnit.DAYS.between(
                    cellData.getValue().getCreatedAt(),
                    LocalDateTime.now()
            );
            return new javafx.beans.property.SimpleStringProperty(days + "");
        });

        // Apply custom cell styling
        colStatus.setCellFactory(col -> createStatusCell());
        colPriority.setCellFactory(col -> createSeverityCell());
        colCategory.setCellFactory(col -> createTypeCell());
        colAssignedTo.setCellFactory(col -> createGroupCell());
        colCreatedAt.setCellFactory(col -> createDaysCell());

        // Load recent tickets
        recentTicketsTable.setItems(FXCollections.observableArrayList(
                ticketList.subList(0, Math.min(10, ticketList.size()))
        ));
    }

    private void setupAllTicketsTable() {
        colAllTicketId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAllTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAllStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAllPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colAllCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colAllAssignedTo.setCellValueFactory(new PropertyValueFactory<>("assignedTo"));
        colAllCreatedAt.setCellValueFactory(cellData -> {
            long days = ChronoUnit.DAYS.between(
                    cellData.getValue().getCreatedAt(),
                    LocalDateTime.now()
            );
            return new javafx.beans.property.SimpleStringProperty(days + "");
        });

        // Apply styling
        colAllStatus.setCellFactory(col -> createStatusCell());
        colAllPriority.setCellFactory(col -> createSeverityCell());
        colAllCategory.setCellFactory(col -> createTypeCell());
        colAllAssignedTo.setCellFactory(col -> createGroupCell());
        colAllCreatedAt.setCellFactory(col -> createDaysCell());

        // Actions column
        colAllActions.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");

            {
                viewBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                viewBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleViewTicket(ticket);
                });

                editBtn.setOnAction(e -> {
                    Ticket ticket = getTableView().getItems().get(getIndex());
                    handleEditTicket(ticket);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, viewBtn, editBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        allTicketsTable.setItems(ticketList);
    }

    // Cell Factories for Custom Styling
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
                    switch (normalized) {
                        case "new":
                            badge.getStyleClass().add("status-new");
                            break;
                        case "open":
                            badge.getStyleClass().add("status-open");
                            break;
                        case "in-progress":
                        case "in_progress":
                            badge.getStyleClass().add("status-progress");
                            break;
                        case "resolved":
                            badge.getStyleClass().add("status-resolved");
                            break;
                        case "closed":
                            badge.getStyleClass().add("status-closed");
                            break;
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        };
    }

    private TableCell<Ticket, String> createSeverityCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(severity);

                    switch (severity.toLowerCase()) {
                        case "low":
                        case "normal":
                            setStyle("-fx-text-fill: #24292e;");
                            break;
                        case "medium":
                            setStyle("-fx-text-fill: #24292e; -fx-font-weight: 500;");
                            break;
                        case "high":
                            setStyle("-fx-text-fill: #d1242f; -fx-font-weight: 600;");
                            break;
                        case "critical":
                            setStyle("-fx-text-fill: #d1242f; -fx-font-weight: 700;");
                            break;
                    }
                }
            }
        };
    }

    private TableCell<Ticket, String> createTypeCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label badge = new Label(type);
                    badge.getStyleClass().add("type-badge");
                    setGraphic(badge);
                    setText(null);
                }
            }
        };
    }

    private TableCell<Ticket, String> createGroupCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String group, boolean empty) {
                super.updateItem(group, empty);
                if (empty || group == null || group.isEmpty()) {
                    setText("Unassigned");
                    setStyle("-fx-text-fill: #57606a;");
                } else {
                    setText(group);
                    getStyleClass().add("group-text");
                }
            }
        };
    }

    private TableCell<Ticket, String> createDaysCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(String days, boolean empty) {
                super.updateItem(days, empty);
                if (empty || days == null) {
                    setText(null);
                } else {
                    setText(days);
                    getStyleClass().add("days-text");
                }
            }
        };
    }

    private void updateDashboardStats() {
        long total = ticketList.size();
        long open = ticketList.stream()
                .filter(t -> "Open".equalsIgnoreCase(t.getStatus()))
                .count();
        long progress = ticketList.stream()
                .filter(t -> "In Progress".equalsIgnoreCase(t.getStatus()) ||
                        "IN_PROGRESS".equalsIgnoreCase(t.getStatus()))
                .count();
        long resolved = ticketList.stream()
                .filter(t -> "Resolved".equalsIgnoreCase(t.getStatus()))
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
        showTicketsView();
    }

    @FXML
    private void handleAllTickets() {
        lblPageTitle.setText("All Tickets");
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
        lblPageTitle.setText("Reports");
        showAlert("Reports", "Reports view coming soon", Alert.AlertType.INFORMATION);
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
        String query = searchField.getText();
        if (!query.isEmpty()) {
            System.out.println("Searching for: " + query);
            // Implement search logic here
        }
    }

    @FXML
    private void handleRefresh() {
        loadTicketsFromDatabase();
        allTicketsTable.refresh();
        recentTicketsTable.refresh();
        updateDashboardStats();
    }

    private void handleViewTicket(Ticket ticket) {
        showAlert("View Ticket",
                "Ticket: " + ticket.getId() + "\n" + ticket.getTitle(),
                Alert.AlertType.INFORMATION);
    }

    private void handleEditTicket(Ticket ticket) {
        openTicketForm(ticket);
    }

    // View Management
    private void showDashboard() {
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        ticketsView.setVisible(false);
        ticketsView.setManaged(false);
        toolbarContainer.setVisible(false);
        toolbarContainer.setManaged(false);
    }

    private void showTicketsView() {
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);
        ticketsView.setVisible(true);
        ticketsView.setManaged(true);
        toolbarContainer.setVisible(true);
        toolbarContainer.setManaged(true);
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

    // Open Forms
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
                    Ticket created = ticketRepository.create(savedTicket);
                    if (created != null) {
                        ticketList.add(0, created);
                    }
                }
                updateDashboardStats();
                recentTicketsTable.refresh();
                allTicketsTable.refresh();
            });

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
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
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("User Management");
            stage.setScene(scene);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open user management", Alert.AlertType.ERROR);
        }
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
        }
    }

    private void openCategoryManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CategoryManagement.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.scene.Scene scene = new javafx.scene.Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            javafx.stage.Stage stage = new javafx.stage.Stage();
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