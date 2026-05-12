package org.example.plugin.impl.note;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import org.example.model.Note;
import org.example.plugin.Plugin;
import org.example.plugin.PluginContext;

public class ExportTxtPlugin implements Plugin {
    @Override
    public String getName() {
        return "Экспорт в TXT";
    }

    @Override
    public void execute(PluginContext context) {
        Note note = context.getSelectedNote();
        if (note == null) {
            showMessage(AlertType.WARNING, "Экспорт", "Сначала выбери заметку для экспорта.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Экспорт заметки");
        fileChooser.setInitialFileName(sanitizeFileName(note.getTitle()) + ".txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовые файлы", "*.txt"));

        var file = fileChooser.showSaveDialog(context.getOwnerWindow());
        if (file == null) {
            return;
        }

        try {
            Files.writeString(Path.of(file.toURI()), note.getContent(), StandardCharsets.UTF_8);
            showMessage(AlertType.INFORMATION, "Экспорт", "Заметка успешно экспортирована.");
        } catch (IOException exception) {
            showMessage(AlertType.ERROR, "Экспорт", "Не удалось экспортировать заметку: " + exception.getMessage());
        }
    }

    private String sanitizeFileName(String value) {
        if (value == null || value.isBlank()) {
            return "zametka";
        }
        return value.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private void showMessage(AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
