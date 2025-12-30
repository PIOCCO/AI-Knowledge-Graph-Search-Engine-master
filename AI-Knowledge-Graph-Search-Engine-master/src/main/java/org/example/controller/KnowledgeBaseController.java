package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.KnowledgeBase;
import org.example.service.SearchService;
import org.example.repository.KBRepository;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Knowledge Base Controller - Article management and semantic search
 */
public class KnowledgeBaseController implements Initializable {

    @FXML
    private TableView<KnowledgeBase> articleTable;
    @FXML
    private TableColumn<KnowledgeBase, String> colTitle;
    @FXML
    private TableColumn<KnowledgeBase, String> colCategory;
    @FXML
    private TableColumn<KnowledgeBase, Integer> colViews;

    @FXML
    private TextField searchField;
    @FXML
    private TextArea articleContentArea;
    @FXML
    private Label articleTitleLabel;

    private final KBRepository kbRepository;
    private final SearchService searchService;
    private ObservableList<KnowledgeBase> articleList;

    public KnowledgeBaseController() {
        this.kbRepository = new KBRepository();
        this.searchService = new SearchService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        loadArticles();

        articleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                displayArticle(newVal);
        });
    }

    private void setupTable() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colViews.setCellValueFactory(new PropertyValueFactory<>("viewCount"));
    }

    private void loadArticles() {
        List<KnowledgeBase> articles = kbRepository.findAll();
        articleList = FXCollections.observableArrayList(articles);
        articleTable.setItems(articleList);
    }

    private void displayArticle(KnowledgeBase article) {
        articleTitleLabel.setText(article.getTitle());
        articleContentArea.setText(article.getContent());

        // Update view count
        article.setViewCount(article.getViewCount() + 1);
        kbRepository.save(article);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        if (query.isEmpty()) {
            loadArticles();
            return;
        }

        List<KnowledgeBase> results = searchService.searchKnowledgeBase(query);
        articleList.setAll(results);
    }

    @FXML
    private void handleNewArticle() {
        // Form to add new article would open here
        showAlert("Article Management", "Open Article Editor Dialog");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
