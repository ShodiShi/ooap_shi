package org.example.plugin.impl.importing;

import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import org.example.plugin.Plugin;
import org.example.plugin.PluginContext;

public class ImportFilePlugin implements Plugin {
    @Override
    public String getName() {
        return "Импорт файла";
    }

    @Override
    public void execute(PluginContext context) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Импорт заметки из файла");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));

        var file = fileChooser.showOpenDialog(context.getOwnerWindow());
        if (file == null) {
            return;
        }

        try {
            context.getImportService().importFromFile(Path.of(file.toURI()));
            showMessage(AlertType.INFORMATION, "Импорт", "Заметка успешно импортирована.");
        } catch (Exception exception) {
            showMessage(AlertType.ERROR, "Импорт", "Не удалось импортировать файл: " + exception.getMessage());
        }
    }

    private void showMessage(AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
