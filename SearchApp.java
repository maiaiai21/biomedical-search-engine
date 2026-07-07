package app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Paper;
import service.*;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class SearchApp extends Application {

    private final SearchEngineService engine = new SearchEngineService();
    private final RecentSearchService recentService = new RecentSearchService();
    private final ExplainabilityService explainer = new ExplainabilityService();

    private ObservableList<Paper> masterData; //informs to the rest when changes
    private FilterService filterService;
    private ListView<Paper> listView;
    private TextField searchField;
    private ComboBox<String> sortBox; //dropdown list button, one at a time
    private TextArea infoArea;
    private Label resultCountLabel;
    private VBox recentBox, explainBox;
    private String currentQuery = "";
    private Paper similaritySource = null;

    @Override
    public void start(Stage stage) {
        List<Paper> loaded = CSVLoader.load("pubmed_small.csv");
        masterData = FXCollections.observableArrayList(loaded);
        filterService = new FilterService(engine, loaded);

        stage.setScene(new Scene(buildUI(), 1150, 850));
        stage.setTitle("Biomedical Research Search Engine");
        stage.show();
    }

    private BorderPane buildUI() {
        // --- SIDEBAR: History & Controls ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(25));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #e2e8f0; -fx-border-width: 0 1 0 0;");
        
        Label sideHeader = new Label("PUBMED EXPLORER");
        sideHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // RESTORED: Sort Selection
        Label sortLabel = new Label("SORT RESULTS BY");
        sortLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        sortBox = new ComboBox<>(FXCollections.observableArrayList("Relevance", "Title (A-Z)"));
        sortBox.setValue("Relevance");
        sortBox.setMaxWidth(Double.MAX_VALUE);
        sortBox.setOnAction(e -> applyFilters()); // Trigger re-sort on change

        Label recentLabel = new Label("RECENT SEARCHES");
        recentLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        
        Button clearRecentBtn = new Button("Clear History");
        clearRecentBtn.setStyle("-fx-font-size: 9px; -fx-text-fill: #ef4444; -fx-background-color: transparent; -fx-cursor: hand;");
        clearRecentBtn.setOnAction(e -> { 
            new File("recent_searches.txt").delete(); 
            refreshRecentSearches(); 
        });

        recentBox = new VBox(6);
        refreshRecentSearches();

        sidebar.getChildren().addAll(sideHeader, new Separator(), sortLabel, sortBox, new Separator(), recentLabel, clearRecentBtn, recentBox);

        // --- TOP: Search Bar ---
        searchField = new TextField();
        searchField.setPromptText("🔍 Search title or abstract...");
        searchField.setPrefHeight(40);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button clearBtn = new Button("✕");
        clearBtn.setPrefHeight(40);
        clearBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b; -fx-cursor: hand; -fx-font-weight: bold;");
        clearBtn.setOnAction(e -> {
            searchField.clear();
            similaritySource = null;
            applyFilters();
        });

        HBox searchBarLayout = new HBox(0, searchField, clearBtn);

        // --- CENTER: Results & Actions ---
        Button recommendBtn = new Button("FIND SIMILAR RESEARCH");
        recommendBtn.setDisable(true);
        recommendBtn.setMaxWidth(Double.MAX_VALUE);
        recommendBtn.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-cursor: hand;");

        listView = new ListView<>(masterData);
        VBox.setVgrow(listView, Priority.ALWAYS);
        setupCellFactory();

        resultCountLabel = new Label("Total: " + masterData.size() + " papers");
        resultCountLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        // --- BOTTOM: Details & Explanation ---
        infoArea = new TextArea("Select a paper to view details...");
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(150);
        infoArea.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px;");

        Button openLinkBtn = new Button("🔗 Open in PubMed");
        openLinkBtn.setDisable(true);
        openLinkBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15 6 15;");
        openLinkBtn.setOnAction(e -> {
            Paper sel = listView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try { java.awt.Desktop.getDesktop().browse(new URI(sel.getLink())); } catch (Exception ex) { }
            }
        });

        explainBox = new VBox(2);
        VBox infoPanel = new VBox(10, new Label("PAPER DETAILS"), infoArea, openLinkBtn, new Separator(), new Label("WHY THIS RESULT?"), explainBox);
        infoPanel.setPadding(new Insets(15));
        infoPanel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-radius: 5;");

        // --- LOGIC ---
        searchField.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (!q.isEmpty()) {
                recentService.save(q);
                similaritySource = null; 
                refreshRecentSearches();
                applyFilters();
            }
        });

        searchField.textProperty().addListener((o, old, val) -> {
            currentQuery = val;
            if (!val.isEmpty()) similaritySource = null; 
            if (similaritySource == null) applyFilters(); 
        });

        recommendBtn.setOnAction(e -> {
            similaritySource = listView.getSelectionModel().getSelectedItem();
            if (similaritySource == null) return;
            currentQuery = ""; 
            searchField.setText(""); 

            List<Paper> similar = masterData.stream()
                .map(p -> { 
                    p.score = (p == similaritySource) ? 1.0 : engine.calculateSimilarity(similaritySource, p); 
                    return p; 
                })
                .sorted((a, b) -> Double.compare(b.score, a.score)).collect(Collectors.toList());
            
            listView.setItems(FXCollections.observableArrayList(similar));
            listView.getSelectionModel().select(0);
            resultCountLabel.setText("Showing results most similar to selected paper");
        });

        listView.getSelectionModel().selectedItemProperty().addListener((o, old, sel) -> {
            if (sel == null) return;
            recommendBtn.setDisable(false);
            openLinkBtn.setDisable(false);
            infoArea.setText("TITLE: " + sel.getTitle() + "\nPMID: " + sel.getPmid() + "\n\nABSTRACT:\n" + sel.getAbstractText());
            updateExplainPanel(sel);
        });

        VBox mainContent = new VBox(12, searchBarLayout, recommendBtn, resultCountLabel, listView, infoPanel);
        mainContent.setPadding(new Insets(20));
        
        BorderPane root = new BorderPane();
        root.setLeft(sidebar); root.setCenter(mainContent);
        return root;
    }

    private void updateExplainPanel(Paper paper) {
        explainBox.getChildren().clear();
        List<ExplainabilityService.TermScore> scores;
        if (!currentQuery.isBlank()) {
            scores = explainer.explain(currentQuery, paper, filterService.getIDFCache());
        } else if (similaritySource != null) {
            String terms = similaritySource.wordCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5).map(Map.Entry::getKey).collect(Collectors.joining(" "));
            scores = explainer.explain(terms, paper, filterService.getIDFCache());
        } else return;

        scores.stream().limit(4).forEach(ts -> {
            Label l = new Label("• Shared: '" + ts.queryTerm() + "' (weight " + String.format("%.4f", ts.score()) + ")");
            l.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");
            explainBox.getChildren().add(l);
        });
    }

    private void applyFilters() {
        // Pass sortBox value to FilterService
        List<Paper> res = filterService.filter(searchField.getText(), null, sortBox.getValue());
        listView.setItems(FXCollections.observableArrayList(res));
        resultCountLabel.setText("Found " + res.size() + " papers");
    }

    private void refreshRecentSearches() {
        recentBox.getChildren().clear();
        recentService.load().forEach(r -> {
            Button b = new Button(r);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color: #e2e8f0; -fx-cursor: hand; -fx-alignment: center-left;");
            b.setOnAction(e -> { 
                searchField.setText(r); 
                similaritySource = null; 
                applyFilters(); 
            });
            recentBox.getChildren().add(b);
        });
    }

    private void setupCellFactory() {
        listView.setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(Paper item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    VBox v = new VBox(2);
                    Label t = new Label(item.getTitle());
                    t.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;");
                    t.setWrapText(true);
                    Label m = new Label("Relevance Score: " + (int)(item.score * 100) + "%");
                    m.setStyle("-fx-font-size: 10px; -fx-text-fill: #94a3b8;");
                    v.getChildren().addAll(t, m);
                    setGraphic(v);
                }
            }
        });
    }

    public static void main(String[] args) { launch(args); }
}