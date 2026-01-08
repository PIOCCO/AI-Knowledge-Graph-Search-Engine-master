package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

import org.example.model.KnowledgeBase;
import org.example.repository.KBRepository;
import org.example.service.AdvancedSearchService;
import org.example.util.SecurityUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Knowledge Base Controller - Manage help articles and documentation
 */
public class KnowledgeBaseController implements Initializable {

    @FXML private TreeView<String> categoryTree;
    @FXML private ListView<KnowledgeBase> articlesList;
    @FXML private TextField searchField;
    @FXML private WebView articleView;
    @FXML private Label articleTitleLabel;
    @FXML private Label articleMetaLabel;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button publishButton;

    // Article Editor
    @FXML private VBox editorPanel;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea contentArea;
    @FXML private TextField tagsField;
    @FXML private CheckBox publishedToggle;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private KBRepository kbRepository;
    private AdvancedSearchService searchService;
    private ObservableList<KnowledgeBase> articles;
    private KnowledgeBase selectedArticle;
    private boolean editMode = false;

    public KnowledgeBaseController() {
        this.kbRepository = new KBRepository();
        this.searchService = new AdvancedSearchService();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCategoryTree();
        setupArticlesList();
        setupEditor();
        loadArticles();
        setupSearch();
        hideEditor();
    }

    private void setupCategoryTree() {
        TreeItem<String> root = new TreeItem<>("Knowledge Base");
        root.setExpanded(true);

        TreeItem<String> gettingStarted = new TreeItem<>("📘 Getting Started");
        gettingStarted.getChildren().addAll(
                new TreeItem<>("Installation"),
                new TreeItem<>("Quick Start Guide"),
                new TreeItem<>("Basic Features")
        );

        TreeItem<String> howTo = new TreeItem<>("📖 How-To Guides");
        howTo.getChildren().addAll(
                new TreeItem<>("Create a Ticket"),
                new TreeItem<>("Manage Categories"),
                new TreeItem<>("Generate Reports")
        );

        TreeItem<String> troubleshooting = new TreeItem<>("🔧 Troubleshooting");
        troubleshooting.getChildren().addAll(
                new TreeItem<>("Connection Issues"),
                new TreeItem<>("Performance Problems"),
                new TreeItem<>("Common Errors")
        );

        TreeItem<String> reference = new TreeItem<>("📚 Reference");
        reference.getChildren().addAll(
                new TreeItem<>("API Documentation"),
                new TreeItem<>("Database Schema"),
                new TreeItem<>("Keyboard Shortcuts")
        );

        root.getChildren().addAll(gettingStarted, howTo, troubleshooting, reference);
        categoryTree.setRoot(root);

        categoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != root) {
                filterArticlesByCategory(newVal.getValue());
            }
        });
    }

    private void setupArticlesList() {
        articlesList.setCellFactory(param -> new ListCell<KnowledgeBase>() {
            @Override
            protected void updateItem(KnowledgeBase article, boolean empty) {
                super.updateItem(article, empty);
                if (empty || article == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label title = new Label(article.getTitle());
                    title.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");

                    Label meta = new Label(
                            (article.isPublished() ? "✅ Published" : "📝 Draft") +
                                    " • " + article.getCategory()
                    );
                    meta.setStyle("-fx-text-fill: #57606a; -fx-font-size: 11px;");

                    box.getChildren().addAll(title, meta);
                    setGraphic(box);
                }
            }
        });

        articlesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayArticle(newVal);
            }
        });
    }

    private void setupEditor() {
        categoryCombo.setItems(FXCollections.observableArrayList(
                "Getting Started", "How-To Guides", "Troubleshooting",
                "Reference", "FAQ", "Release Notes"
        ));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                searchArticles(newVal);
            } else {
                loadArticles();
            }
        });
    }

    private void loadArticles() {
        try {
            List<KnowledgeBase> allArticles = kbRepository.findAll();
            articles = FXCollections.observableArrayList(allArticles);
            articlesList.setItems(articles);
            System.out.println("✅ Loaded " + allArticles.size() + " KB articles");
        } catch (Exception e) {
            System.err.println("❌ Error loading articles: " + e.getMessage());
            articles = FXCollections.observableArrayList();
        }
    }

    private void searchArticles(String query) {
        try {
            List<KnowledgeBase> results = searchService.searchKnowledgeBase(query);
            articlesList.setItems(FXCollections.observableArrayList(results));
        } catch (Exception e) {
            System.err.println("Error searching articles: " + e.getMessage());
        }
    }

    private void filterArticlesByCategory(String category) {
        if (articles == null) return;

        String cleanCategory = category.replaceAll("^[📘📖🔧📚]\\s+", "");

        List<KnowledgeBase> filtered = articles.stream()
                .filter(a -> a.getCategory() != null && a.getCategory().contains(cleanCategory))
                .toList();

        articlesList.setItems(FXCollections.observableArrayList(filtered));
    }

    private void displayArticle(KnowledgeBase article) {
        selectedArticle = article;
        articleTitleLabel.setText(article.getTitle());

        String meta = String.format("Category: %s | Author: %s | Updated: %s",
                article.getCategory(),
                article.getAuthorName() != null ? article.getAuthorName() : "Unknown",
                article.getUpdatedAt() != null ? article.getUpdatedAt().toString() : "N/A"
        );
        articleMetaLabel.setText(meta);

        String htmlContent = generateHtmlContent(article);
        articleView.getEngine().loadContent(htmlContent);

        editButton.setDisable(false);
        deleteButton.setDisable(false);
        publishButton.setDisable(false);
    }

    private String generateHtmlContent(KnowledgeBase article) {
        return "<!DOCTYPE html><html><head>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Arial, sans-serif; padding: 20px; line-height: 1.6; }" +
                "h1 { color: #24292e; border-bottom: 2px solid #0969da; padding-bottom: 10px; }" +
                "h2 { color: #24292e; margin-top: 24px; }" +
                "code { background: #f6f8fa; padding: 2px 6px; border-radius: 3px; }" +
                "pre { background: #f6f8fa; padding: 16px; border-radius: 6px; overflow-x: auto; }" +
                "</style></head><body>" +
                "<h1>" + article.getTitle() + "</h1>" +
                "<div style='color: #57606a; margin-bottom: 20px;'>" +
                "Category: " + article.getCategory() + " | " +
                "Tags: " + (article.getTags() != null ? String.join(", ", article.getTags()) : "None") +
                "</div>" +
                "<div>" + formatContent(article.getContent()) + "</div>" +
                "</body></html>";
    }

    private String formatContent(String content) {
        if (content == null) return "";

        // Simple markdown-like formatting
        return content
                .replaceAll("\\n", "<br/>")
                .replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("\\*(.*?)\\*", "<em>$1</em>")
                .replaceAll("`(.*?)`", "<code>$1</code>");
    }

    @FXML
    private void handleNewArticle() {
        editMode = false;
        showEditor();
        clearEditorFields();
        titleField.requestFocus();
    }

    @FXML
    private void handleEditArticle() {
        if (selectedArticle == null) {
            showAlert("No Selection", "Please select an article to edit", Alert.AlertType.WARNING);
            return;
        }

        editMode = true;
        showEditor();
        populateEditorFields(selectedArticle);
    }

    @FXML
    private void handleDeleteArticle() {
        if (selectedArticle == null) {
            showAlert("No Selection", "Please select an article to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Article");
        confirm.setHeaderText("Delete: " + selectedArticle.getTitle());
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    kbRepository.delete(selectedArticle.getId());
                    loadArticles();
                    articleView.getEngine().loadContent("");
                    showAlert("Success", "Article deleted successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handlePublishArticle() {
        if (selectedArticle == null) return;

        selectedArticle.setPublished(!selectedArticle.isPublished());
        selectedArticle.setUpdatedAt(LocalDateTime.now());

        try {
            kbRepository.save(selectedArticle);
            loadArticles();
            displayArticle(selectedArticle);

            String status = selectedArticle.isPublished() ? "published" : "unpublished";
            showAlert("Success", "Article " + status + " successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to update status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveArticle() {
        String title = titleField.getText().trim();
        String category = categoryCombo.getValue();
        String content = contentArea.getText().trim();

        if (title.isEmpty() || category == null || content.isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields", Alert.AlertType.WARNING);
            return;
        }

        try {
            KnowledgeBase article;

            if (editMode && selectedArticle != null) {
                article = selectedArticle;
                article.setUpdatedAt(LocalDateTime.now());
            } else {
                article = new KnowledgeBase();
                article.setId(SecurityUtils.generateId());
                article.setCreatedAt(LocalDateTime.now());
                article.setAuthorId("current-user-id");
                article.setAuthorName("Current User");
            }

            article.setTitle(title);
            article.setCategory(category);
            article.setContent(content);
            article.setPublished(publishedToggle.isSelected());

            String[] tags = tagsField.getText().split(",");
            article.setTags(java.util.Arrays.asList(tags));

            kbRepository.save(article);
            loadArticles();
            hideEditor();

            showAlert("Success", "Article saved successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to save: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelEdit() {
        hideEditor();
    }

    private void showEditor() {
        editorPanel.setVisible(true);
        editorPanel.setManaged(true);
    }

    private void hideEditor() {
        editorPanel.setVisible(false);
        editorPanel.setManaged(false);
    }

    private void clearEditorFields() {
        titleField.clear();
        categoryCombo.setValue(null);
        contentArea.clear();
        tagsField.clear();
        publishedToggle.setSelected(false);
    }

    private void populateEditorFields(KnowledgeBase article) {
        titleField.setText(article.getTitle());
        categoryCombo.setValue(article.getCategory());
        contentArea.setText(article.getContent());
        tagsField.setText(article.getTags() != null ? String.join(", ", article.getTags()) : "");
        publishedToggle.setSelected(article.isPublished());
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}