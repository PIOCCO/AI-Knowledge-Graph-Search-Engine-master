package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.Team;
import org.example.repository.TeamRepository;
import org.example.service.UserService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Team Controller - Management of support teams and agent assignments
 */
public class TeamController implements Initializable {

    @FXML
    private TableView<Team> teamTable;
    @FXML
    private TableColumn<Team, String> colTeamId;
    @FXML
    private TableColumn<Team, String> colTeamName;
    @FXML
    private TableColumn<Team, String> colDepartment;
    @FXML
    private TableColumn<Team, String> colLead;

    @FXML
    private TextField teamNameField;
    @FXML
    private TextField departmentField;
    @FXML
    private ComboBox<String> leadSelector;

    private final TeamRepository teamRepository;
    private final UserService userService;
    private ObservableList<Team> teamList;

    public TeamController() {
        this.teamRepository = new TeamRepository();
        this.userService = new UserService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadTeams();
        loadAgents();
    }

    private void setupTable() {
        colTeamId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTeamName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colLead.setCellValueFactory(new PropertyValueFactory<>("leadId"));
    }

    private void loadTeams() {
        List<Team> teams = teamRepository.findAll();
        teamList = FXCollections.observableArrayList(teams);
        teamTable.setItems(teamList);
    }

    private void loadAgents() {
        if (leadSelector != null) {
            userService.getAllUsers().forEach(u -> leadSelector.getItems().add(u.getUsername()));
        }
    }

    @FXML
    private void handleAddTeam() {
        String name = teamNameField.getText();
        String dept = departmentField.getText();
        String lead = leadSelector.getValue();

        if (name.isEmpty() || dept.isEmpty()) {
            showAlert("Required", "Please fill in all fields");
            return;
        }

        Team team = new Team();
        team.setId("TEAM-" + System.currentTimeMillis() % 1000);
        team.setName(name);
        team.setDepartment(dept);
        team.setLeadId(lead);

        teamRepository.save(team);
        teamList.add(team);
        clearFields();
        showAlert("Success", "Team created successfully!");
    }

    @FXML
    private void handleDeleteTeam() {
        Team selected = teamTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            teamRepository.delete(selected.getId());
            teamList.remove(selected);
            showAlert("Deleted", "Team removed from system");
        }
    }

    private void clearFields() {
        teamNameField.clear();
        departmentField.clear();
        leadSelector.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
