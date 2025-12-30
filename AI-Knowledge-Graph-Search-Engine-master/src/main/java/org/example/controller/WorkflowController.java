package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.Workflow;
import org.example.service.WorkflowEngine;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Workflow Controller - Builder for automation rules and ticket lifecycle
 * triggers
 */
public class WorkflowController implements Initializable {

    @FXML
    private TableView<Workflow> workflowTable;
    @FXML
    private TableColumn<Workflow, String> colName;
    @FXML
    private TableColumn<Workflow, String> colTrigger;
    @FXML
    private TableColumn<Workflow, Boolean> colActive;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> triggerCombo;
    @FXML
    private TextArea ruleDescription;

    private final WorkflowEngine workflowEngine;

    public WorkflowController() {
        this.workflowEngine = new WorkflowEngine();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadWorkflows();
        setupTriggers();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTrigger.setCellValueFactory(new PropertyValueFactory<>("triggerEvent"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
    }

    private void setupTriggers() {
        triggerCombo.getItems().setAll(
                "TICKET_CREATED",
                "TICKET_ASSIGNED",
                "TICKET_RESOLVED",
                "SLA_BREACHED",
                "PRIORITY_CHANGED");
    }

    private void loadWorkflows() {
        ObservableList<Workflow> data = FXCollections.observableArrayList(workflowEngine.getAllWorkflows());
        workflowTable.setItems(data);
    }

    @FXML
    private void handleCreateWorkflow() {
        String name = nameField.getText();
        String trigger = triggerCombo.getValue();

        if (name.isEmpty() || trigger == null) {
            showAlert("Required", "Provide name and trigger event");
            return;
        }

        workflowEngine.createWorkflow(name, trigger, "SYSTEM");
        loadWorkflows();
        nameField.clear();
        showAlert("Workflow Created", "Rule added to automation engine");
    }

    @FXML
    private void handleToggleActive() {
        Workflow selected = workflowTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (selected.isActive())
                workflowEngine.deactivateWorkflow(selected.getId());
            else
                workflowEngine.activateWorkflow(selected.getId());
            loadWorkflows();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
