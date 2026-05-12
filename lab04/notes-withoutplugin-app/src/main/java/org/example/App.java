package org.example;

import java.util.Objects;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.db.DatabaseManager;
import org.example.repository.CategoryRepository;
import org.example.repository.ImportRepository;
import org.example.repository.NoteRepository;
import org.example.service.ImportService;
import org.example.ui.MainController;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.initDatabase();

        CategoryRepository categoryRepository = new CategoryRepository(databaseManager);
        categoryRepository.insertDefaultCategories();

        NoteRepository noteRepository = new NoteRepository(databaseManager);
        ImportRepository importRepository = new ImportRepository(databaseManager);
        ImportService importService = new ImportService(noteRepository, importRepository, categoryRepository);

        MainController controller = new MainController(
                stage,
                noteRepository,
                categoryRepository,
                importService
        );

        Scene scene = new Scene(controller.createView(), 1100, 650);
        scene.getStylesheets().add(Objects.requireNonNull(
                App.class.getResource("/styles/app.css")
        ).toExternalForm());
        stage.setTitle("Заметки без паттерна");
        stage.setScene(scene);
        stage.setMinWidth(980);
        stage.setMinHeight(620);
        stage.show();
        Platform.runLater(controller::playEntranceAnimation);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
