package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;

import org.example.model.Category;
import org.example.repository.CategoryRepository;
import org.example.util.SecurityUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CategoryController implements Initializable {

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category, String> colCategoryId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colDescription;
    @FXML private TableColumn<Category, String> colColor;
    @FXML private TableColumn<Category, Integer> colTicketCount;
    @FXML private TableColumn<Category, Void> colActions;
    @FXML private TextField searchField;

    private CategoryRepository categoryRepository;
    private ObservableList<Category> categoryList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        categoryRepository = new CategoryRepository();
        setupTable();
        loadCategories();
    }

    private void setupTable() {
        colCategoryId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));
        colTicketCount.setCellValueFactory(new PropertyValueFactory<>("ticketCount"));

        // Color cell with visual indicator
        colColor.setCellFactory(col -> new TableCell<Category, String>() {
            @Override
            protected void updateItem(String color, boolean empty) {
                super.updateItem(color, empty);
                if (empty || color == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);

                    Label colorBox = new Label("  ");
                    colorBox.setStyle("-fx-background-color: " + color + "; " +
                            "-fx-border-color: #d0d7de; -fx-border-width: 1; " +
                            "-fx-min-width: 30; -fx-min-height: 20;");

                    Label colorText = new Label(color);
                    box.getChildren().addAll(colorBox, colorText);
                    setGraphic(box);
                    setText(null);
                }
            }
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<Category, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                editBtn.setOnAction(e -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleEditCategory(category);
                });

                deleteBtn.setOnAction(e -> {
                    Category category = getTableView().getItems().get(getIndex());
                    handleDeleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadCategories() {
        List<Category> categories = categoryRepository.findAll();
        categoryList = FXCollections.observableArrayList(categories);
        categoryTable.setItems(categoryList);
    }

    @FXML
    private void handleAddCategory() {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Add New Category");
        dialog.setHeaderText("Create a new ticket category");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Form fields
        TextField nameField = new TextField();
        nameField.setPromptText("Category Name");

        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        ColorPicker colorPicker = new ColorPicker(Color.web("#3498db"));

        TextField iconField = new TextField();
        iconField.setPromptText("Icon (emoji or text)");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        grid.add(new Label("Icon:"), 0, 3);
        grid.add(iconField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Category category = new Category();
                category.setId(SecurityUtils.generateId());
                category.setName(nameField.getText());
                category.setDescription(descField.getText());
                category.setColor(toHexString(colorPicker.getValue()));
                category.setIcon(iconField.getText());
                return category;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(category -> {
            categoryRepository.save(category);
            categoryList.add(category);
            showAlert("Success", "Category created successfully!", Alert.AlertType.INFORMATION);
        });
    }

    private void handleEditCategory(Category category) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Edit Category");
        dialog.setHeaderText("Edit category: " + category.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField(category.getName());
        TextArea descField = new TextArea(category.getDescription());
        descField.setPrefRowCount(3);

        ColorPicker colorPicker = new ColorPicker(
                category.getColor() != null ? Color.web(category.getColor()) : Color.web("#3498db")
        );

        TextField iconField = new TextField(category.getIcon());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Color:"), 0, 2);
        grid.add(colorPicker, 1, 2);
        grid.add(new Label("Icon:"), 0, 3);
        grid.add(iconField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                category.setName(nameField.getText());
                category.setDescription(descField.getText());
                category.setColor(toHexString(colorPicker.getValue()));
                category.setIcon(iconField.getText());
                return category;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedCategory -> {
            categoryRepository.save(updatedCategory);
            categoryTable.refresh();
            showAlert("Success", "Category updated successfully!", Alert.AlertType.INFORMATION);
        });
    }

    private void handleDeleteCategory(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete " + category.getName() + "?");
        alert.setContentText("This action cannot be undone. Tickets in this category will need reassignment.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                categoryRepository.delete(category.getId());
                categoryList.remove(category);
                showAlert("Success", "Category deleted successfully!", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            categoryTable.setItems(categoryList);
        } else {
            ObservableList<Category> filtered = categoryList.filtered(cat ->
                    cat.getName().toLowerCase().contains(query) ||
                            (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(query))
            );
            categoryTable.setItems(filtered);
        }
    }

    @FXML
    private void handleRefresh() {
        loadCategories();
        showAlert("Refresh", "Category list refreshed", Alert.AlertType.INFORMATION);
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}