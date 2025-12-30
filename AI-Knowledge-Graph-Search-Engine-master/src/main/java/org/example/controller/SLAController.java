package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.SLA;
import org.example.model.enums.Priority;
import org.example.service.SLAService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * SLA Controller - Management of Service Level Agreements and response targets
 */
public class SLAController implements Initializable {

    @FXML
    private TableView<SLA> slaTable;
    @FXML
    private TableColumn<SLA, String> colName;
    @FXML
    private TableColumn<SLA, Priority> colPriority;
    @FXML
    private TableColumn<SLA, Integer> colResponse;
    @FXML
    private TableColumn<SLA, Integer> colResolution;

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<Priority> priorityCombo;
    @FXML
    private Spinner<Integer> responseTimeSpinner;
    @FXML
    private Spinner<Integer> resolutionTimeSpinner;

    private final SLAService slaService;

    public SLAController() {
        this.slaService = new SLAService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadSLAs();
        setupInputs();
    }

    private void setupTable() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPriority.setCellValueFactory(new PropertyValueFactory<>("priority"));
        colResponse.setCellValueFactory(new PropertyValueFactory<>("responseTimeMinutes"));
        colResolution.setCellValueFactory(new PropertyValueFactory<>("resolutionTimeMinutes"));
    }

    private void setupInputs() {
        priorityCombo.getItems().setAll(Priority.values());
        responseTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10080, 240));
        resolutionTimeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 43200, 1440));
    }

    private void loadSLAs() {
        ObservableList<SLA> data = FXCollections.observableArrayList(slaService.getAllSLAs());
        slaTable.setItems(data);
    }

    @FXML
    private void handleCreateSLA() {
        String name = nameField.getText();
        Priority priority = priorityCombo.getValue();
        int resp = responseTimeSpinner.getValue();
        int res = resolutionTimeSpinner.getValue();

        if (name.isEmpty() || priority == null) {
            showAlert("Required", "Please provide a name and priority");
            return;
        }

        slaService.createSLA(name, priority, resp, res);
        loadSLAs();
        nameField.clear();
        showAlert("Success", "SLA Policy created");
    }

    @FXML
    private void handleDeleteSLA() {
        SLA selected = slaTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            slaService.deleteSLA(selected.getId());
            loadSLAs();
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
