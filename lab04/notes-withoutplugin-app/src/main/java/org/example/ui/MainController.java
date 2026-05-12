package org.example.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.model.Category;
import org.example.model.Note;
import org.example.repository.CategoryRepository;
import org.example.repository.NoteRepository;
import org.example.service.ImportService;

public class MainController {
    private final Stage stage;
    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;
    private final ImportService importService;

    private final ListView<Note> notesListView = new ListView<>();
    private final TextField titleField = new TextField();
    private final TextArea contentArea = new TextArea();
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final TextField searchField = new TextField();
    private final Label notesCountLabel = new Label("0 заметок");
    private final Label categoryStatusLabel = new Label("Нет категории");
    private final Label currentNoteStatusLabel = new Label("Заметка не выбрана");
    private final Label editorModeLabel = new Label("Готово к созданию");
    private final List<Note> allNotes = new ArrayList<>();
    private final List<Button> toolButtons = new ArrayList<>();
    private Parent heroSection;
    private VBox notesBox;
    private VBox editorBox;
    private VBox toolsBox;
    private Note currentNote;

    public MainController(
            Stage stage,
            NoteRepository noteRepository,
            CategoryRepository categoryRepository,
            ImportService importService
    ) {
        this.stage = stage;
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
        this.importService = importService;
    }

    public Parent createView() {
        BorderPane workspace = new BorderPane();
        workspace.getStyleClass().add("workspace");

        notesListView.setPrefWidth(260);
        notesListView.getStyleClass().add("notes-list");
        notesListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> showSelectedNote(newValue));
        notesListView.setCellFactory(listView -> new NoteListCell());

        searchField.setPromptText("Найти заметку по заголовку...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());

        titleField.setPromptText("Введи понятный заголовок заметки");
        contentArea.setPromptText("Напиши здесь текст заметки...");
        contentArea.setWrapText(true);

        editorBox = new VBox(14);
        editorBox.getStyleClass().add("panel-card");
        editorBox.getChildren().addAll(
                createPanelTitle("Редактор", "Обновляй выбранную заметку или создавай новую"),
                createStatusPills(),
                new Label("Заголовок"),
                titleField,
                new Label("Категория"),
                categoryComboBox,
                new Label("Содержимое"),
                contentArea,
                createActionButtons()
        );
        contentArea.setPrefRowCount(18);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        toolsBox = buildDirectTools();
        toolsBox.setPrefWidth(260);

        notesBox = new VBox(
                14,
                createPanelTitle("Библиотека", "Ищи и просматривай сохраненные заметки"),
                searchField,
                notesListView
        );
        notesBox.getStyleClass().add("panel-card");
        VBox.setVgrow(notesListView, Priority.ALWAYS);

        workspace.setLeft(notesBox);
        workspace.setCenter(editorBox);
        workspace.setRight(toolsBox);
        BorderPane.setMargin(notesBox, new Insets(0, 18, 0, 0));
        BorderPane.setMargin(toolsBox, new Insets(0, 0, 0, 18));

        heroSection = createHeroSection();
        VBox root = new VBox(18, heroSection, workspace);
        root.getStyleClass().add("app-root");
        root.setPadding(new Insets(20));
        VBox.setVgrow(workspace, Priority.ALWAYS);

        loadCategories();
        loadNotes();
        clearEditor();

        return root;
    }

    private HBox createActionButtons() {
        Button newButton = createButton("Новая", "secondary-button");
        newButton.setOnAction(event -> createNote());

        Button saveButton = createButton("Сохранить", "primary-button");
        saveButton.setOnAction(event -> editNote());

        Button deleteButton = createButton("Удалить", "danger-button");
        deleteButton.setOnAction(event -> deleteNote());

        HBox buttons = new HBox(10, newButton, saveButton, deleteButton);
        buttons.setAlignment(Pos.CENTER_LEFT);
        return buttons;
    }

    private VBox buildDirectTools() {
        VBox box = new VBox(12);
        box.getStyleClass().add("panel-card");
        box.getChildren().add(createPanelTitle("Функции", ""));

        addToolButton(box, "Статистика", this::showStatistics);
        addToolButton(box, "Экспорт в TXT", this::exportTxt);
        addToolButton(box, "Импорт файла", this::importFromFile);
        addToolButton(box, "Импорт папки", this::importFromFolder);
        box.setFillWidth(true);
        return box;
    }

    private void addToolButton(VBox box, String text, Runnable action) {
        Button button = createButton(text, "tool-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(event -> {
            action.run();
            loadNotes();
        });
        box.getChildren().add(button);
        toolButtons.add(button);
    }

    private Button createButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        return button;
    }

    private void loadCategories() {
        List<Category> categories = categoryRepository.findAll();
        categoryComboBox.getItems().setAll(categories);
        if (!categories.isEmpty() && categoryComboBox.getValue() == null) {
            categoryComboBox.setValue(categories.get(0));
        }
        updateDashboard();
    }

    private void showSelectedNote(Note note) {
        if (note == null) {
            currentNote = null;
            titleField.clear();
            contentArea.clear();
            if (!categoryComboBox.getItems().isEmpty()) {
                categoryComboBox.setValue(categoryComboBox.getItems().get(0));
            }
            editorModeLabel.setText("Готово к созданию");
            currentNoteStatusLabel.setText("Заметка не выбрана");
            updateDashboard();
            return;
        }

        currentNote = note;
        titleField.setText(note.getTitle());
        contentArea.setText(note.getContent());
        selectCategory(note.getCategoryId());
        editorModeLabel.setText("Редактирование заметки");
        currentNoteStatusLabel.setText(shorten(note.getTitle(), 24));
        updateDashboard();
    }

    private void selectCategory(int categoryId) {
        for (Category category : categoryComboBox.getItems()) {
            if (category.getId() == categoryId) {
                categoryComboBox.setValue(category);
                return;
            }
        }
    }

    private void saveNote() {
        Category category = categoryComboBox.getValue();
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String content = contentArea.getText() == null ? "" : contentArea.getText().trim();

        if (title.isBlank()) {
            showMessage(AlertType.WARNING, "Проверка", "Заголовок обязателен.");
            return;
        }

        if (category == null) {
            showMessage(AlertType.WARNING, "Проверка", "Нужно выбрать категорию.");
            return;
        }

        Note note = currentNote == null ? new Note() : currentNote;
        note.setTitle(title);
        note.setContent(content);
        note.setCategoryId(category.getId());
        noteRepository.save(note);

        loadNotes();
        selectNoteById(note.getId());
        currentNote = note;
        editorModeLabel.setText("Редактирование заметки");
        currentNoteStatusLabel.setText(shorten(note.getTitle(), 24));
        showMessage(AlertType.INFORMATION, "Сохранение", "Заметка сохранена.");
    }

    public void createNote() {
        clearEditor();
    }

    public void editNote() {
        saveNote();
    }

    public void deleteNote() {
        if (currentNote == null || currentNote.getId() == 0) {
            showMessage(AlertType.WARNING, "Удаление", "Сначала выбери заметку для удаления.");
            return;
        }

        noteRepository.delete(currentNote.getId());
        clearEditor();
        loadNotes();
        showMessage(AlertType.INFORMATION, "Удаление", "Заметка удалена.");
    }

    public void loadNotes() {
        allNotes.clear();
        allNotes.addAll(noteRepository.findAll());
        applyFilter();
        updateDashboard();
    }

    private void showStatistics() {
        if (currentNote == null) {
            showMessage(AlertType.WARNING, "Статистика", "Сначала выбери заметку.");
            return;
        }

        String content = currentNote.getContent() == null ? "" : currentNote.getContent();
        int characters = content.length();
        int words = content.isBlank() ? 0 : content.trim().split("\\s+").length;
        int lines = content.isBlank() ? 0 : content.split("\\R", -1).length;

        showMessage(
                AlertType.INFORMATION,
                "Статистика",
                "Заголовок: " + currentNote.getTitle()
                        + "\nСимволы: " + characters
                        + "\nСлова: " + words
                        + "\nСтроки: " + lines
        );
    }

    private void exportTxt() {
        if (currentNote == null) {
            showMessage(AlertType.WARNING, "Экспорт", "Сначала выбери заметку для экспорта.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт заметки");
        fileChooser.setInitialFileName(sanitizeFileName(currentNote.getTitle()) + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));

        var file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            Files.writeString(Path.of(file.toURI()), currentNote.getContent(), StandardCharsets.UTF_8);
            showMessage(AlertType.INFORMATION, "Экспорт", "Заметка успешно экспортирована.");
        } catch (IOException exception) {
            showMessage(AlertType.ERROR, "Экспорт", "Не удалось экспортировать заметку: " + exception.getMessage());
        }
    }

    private void importFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Импорт заметки из файла");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));

        var file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            importService.importFromFile(Path.of(file.toURI()));
            showMessage(AlertType.INFORMATION, "Импорт", "Заметка успешно импортирована.");
        } catch (Exception exception) {
            showMessage(AlertType.ERROR, "Импорт", "Не удалось импортировать файл: " + exception.getMessage());
        }
    }

    private void importFromFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Импорт заметок из папки");

        var directory = directoryChooser.showDialog(stage);
        if (directory == null) {
            return;
        }

        try {
            int importedCount = importService.importFromFolder(Path.of(directory.toURI()));
            showMessage(AlertType.INFORMATION, "Импорт", "Импортировано файлов: " + importedCount);
        } catch (Exception exception) {
            showMessage(AlertType.ERROR, "Импорт", "Не удалось импортировать папку: " + exception.getMessage());
        }
    }

    private String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "zametka";
        }
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private void clearEditor() {
        currentNote = null;
        titleField.clear();
        contentArea.clear();
        if (!categoryComboBox.getItems().isEmpty()) {
            categoryComboBox.setValue(categoryComboBox.getItems().get(0));
        }
        notesListView.getSelectionModel().clearSelection();
        editorModeLabel.setText("Готово к созданию");
        currentNoteStatusLabel.setText("Заметка не выбрана");
        updateDashboard();
    }

    private void selectNoteById(int id) {
        for (Note note : notesListView.getItems()) {
            if (note.getId() == id) {
                notesListView.getSelectionModel().select(note);
                return;
            }
        }
    }

    public void playEntranceAnimation() {
        if (heroSection == null || notesBox == null || editorBox == null || toolsBox == null) {
            return;
        }

        heroSection.setOpacity(0);
        notesBox.setOpacity(0);
        editorBox.setOpacity(0);
        toolsBox.setOpacity(0);

        heroSection.setTranslateY(-46);
        notesBox.setTranslateX(-60);
        editorBox.setTranslateY(56);
        toolsBox.setTranslateX(60);

        ParallelTransition layoutIntro = new ParallelTransition(
                buildCardIntro(heroSection, 1.0, 1.0, 560),
                buildCardIntro(notesBox, 0.97, 1.0, 680),
                buildCardIntro(editorBox, 0.95, 1.0, 760),
                buildCardIntro(toolsBox, 0.95, 1.0, 860)
        );

        SequentialTransition buttonsIntro = new SequentialTransition();
        for (Button button : toolButtons) {
            button.setOpacity(0);
            button.setTranslateX(26);
            button.setScaleX(0.92);
            button.setScaleY(0.92);
            buttonsIntro.getChildren().add(buildButtonIntro(button, 260));
        }

        layoutIntro.play();
        buttonsIntro.playFrom(Duration.millis(500));
    }

    private void showMessage(AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.initOwner(stage);
        alert.showAndWait();
    }

    private Parent createHeroSection() {
        VBox textBlock = new VBox(6);
        Label badge = new Label("БЕЗ ПАТТЕРНА");
        badge.getStyleClass().add("hero-badge");

        Label title = new Label("Заметки");
        title.getStyleClass().add("hero-title");

        textBlock.getChildren().addAll(badge, title);

        HBox stats = new HBox(
                12,
                createStatCard("Заметки", notesCountLabel),
                createStatCard("Категория", categoryStatusLabel),
                createStatCard("Выбор", currentNoteStatusLabel)
        );
        stats.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox hero = new HBox(18, textBlock, spacer, stats);
        hero.getStyleClass().add("hero-card");
        hero.setAlignment(Pos.CENTER_LEFT);
        return hero;
    }

    private VBox createStatCard(String labelText, Label valueLabel) {
        Label label = new Label(labelText);
        label.getStyleClass().add("stat-card-label");
        valueLabel.getStyleClass().add("stat-card-value");

        VBox card = new VBox(4, label, valueLabel);
        card.getStyleClass().add("stat-card");
        card.setMinWidth(140);
        return card;
    }

    private VBox createPanelTitle(String titleText, String subtitleText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("panel-title");

        VBox titleBlock = new VBox(4, title);
        if (subtitleText == null || subtitleText.isBlank()) {
            return titleBlock;
        }

        Label subtitle = new Label(subtitleText);
        subtitle.getStyleClass().add("panel-subtitle");
        subtitle.setWrapText(true);

        titleBlock.getChildren().add(subtitle);
        return titleBlock;
    }

    private HBox createStatusPills() {
        Label editorLabel = new Label("Режим");
        editorLabel.getStyleClass().add("pill-label");
        editorModeLabel.getStyleClass().add("pill-value");

        VBox pillA = new VBox(2, editorLabel, editorModeLabel);
        pillA.getStyleClass().add("info-pill");

        return new HBox(10, pillA);
    }

    private void applyFilter() {
        String filter = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        List<Note> filteredNotes = allNotes.stream()
                .filter(note -> filter.isBlank() || safeTitle(note).toLowerCase().contains(filter))
                .toList();

        notesListView.getItems().setAll(filteredNotes);

        if (currentNote != null) {
            selectNoteById(currentNote.getId());
        }

        notesCountLabel.setText(filteredNotes.size() + endingForNotes(filteredNotes.size()));
    }

    private void updateDashboard() {
        if (currentNote != null) {
            currentNoteStatusLabel.setText(shorten(currentNote.getTitle(), 24));
        } else {
            currentNoteStatusLabel.setText("Заметка не выбрана");
        }

        Category category = categoryComboBox.getValue();
        categoryStatusLabel.setText(category == null ? "Нет категории" : shorten(category.getName(), 18));
    }

    private String endingForNotes(int count) {
        return count == 1 ? " заметка" : " заметок";
    }

    private String shorten(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "Без названия";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String safeTitle(Note note) {
        String title = note.getTitle();
        return title == null || title.isBlank() ? "Заметка без названия" : title;
    }

    private ParallelTransition buildCardIntro(Node node, double fromScale, double toScale, int durationMs) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMs), node);
        translate.setToX(0);
        translate.setToY(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(fromScale);
        scale.setFromY(fromScale);
        scale.setToX(toScale);
        scale.setToY(toScale);

        return new ParallelTransition(fade, translate, scale);
    }

    private ParallelTransition buildButtonIntro(Button button, int durationMs) {
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), button);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(durationMs), button);
        translate.setFromX(26);
        translate.setToX(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), button);
        scale.setFromX(0.92);
        scale.setFromY(0.92);
        scale.setToX(1);
        scale.setToY(1);

        return new ParallelTransition(fade, translate, scale);
    }

    private static class NoteListCell extends ListCell<Note> {
        @Override
        protected void updateItem(Note note, boolean empty) {
            super.updateItem(note, empty);

            if (empty || note == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(
                    note.getTitle() == null || note.getTitle().isBlank()
                            ? "Заметка без названия"
                            : note.getTitle()
            );
            title.getStyleClass().add("note-cell-title");

            Label preview = new Label(buildPreview(note.getContent()));
            preview.getStyleClass().add("note-cell-preview");
            preview.setWrapText(true);

            VBox content = new VBox(4, title, preview);
            content.getStyleClass().add("note-cell-box");

            StackPane wrapper = new StackPane(content);
            wrapper.setPadding(new Insets(4, 0, 4, 0));

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setGraphic(wrapper);
        }

        private String buildPreview(String content) {
            if (content == null || content.isBlank()) {
                return "Пустая заметка";
            }

            String normalized = content.replaceAll("\\s+", " ").trim();
            if (normalized.length() <= 70) {
                return normalized;
            }
            return normalized.substring(0, 67) + "...";
        }
    }
}
