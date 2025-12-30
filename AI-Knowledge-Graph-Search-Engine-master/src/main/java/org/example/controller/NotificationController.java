package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.Notification;
import org.example.service.NotificationService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Notification Controller - Real-time alerts and user message history
 */
public class NotificationController implements Initializable {

    @FXML
    private TableView<Notification> notificationTable;
    @FXML
    private TableColumn<Notification, String> colMessage;
    @FXML
    private TableColumn<Notification, String> colTime;
    @FXML
    private TableColumn<Notification, Boolean> colRead;

    private final NotificationService notificationService;

    public NotificationController() {
        this.notificationService = new NotificationService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadNotifications();
    }

    private void setupTable() {
        colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colRead.setCellValueFactory(new PropertyValueFactory<>("read"));
    }

    private void loadNotifications() {
        // Assuming getting current user would be from a session
        String userId = "current-user-id";
        ObservableList<Notification> data = FXCollections.observableArrayList(
                notificationService.getUserNotifications(userId));
        notificationTable.setItems(data);
    }

    @FXML
    private void handleMarkAsRead() {
        Notification selected = notificationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            notificationService.markAsRead(selected.getId());
            loadNotifications();
        }
    }

    @FXML
    private void handleClearAll() {
        String userId = "current-user-id";
        notificationService.clearAll(userId);
        loadNotifications();
    }
}
