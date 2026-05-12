package org.example.plugin.impl.note;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.example.model.Note;
import org.example.plugin.Plugin;
import org.example.plugin.PluginContext;

public class StatisticsPlugin implements Plugin {
    @Override
    public String getName() {
        return "Статистика";
    }

    @Override
    public void execute(PluginContext context) {
        Note note = context.getSelectedNote();
        if (note == null) {
            showMessage(AlertType.WARNING, "Статистика", "Сначала выбери заметку.");
            return;
        }

        String content = note.getContent() == null ? "" : note.getContent();
        int characters = content.length();
        int words = content.isBlank() ? 0 : content.trim().split("\\s+").length;
        int lines = content.isBlank() ? 0 : content.split("\\R", -1).length;

        showMessage(
                AlertType.INFORMATION,
                "Статистика",
                "Заголовок: " + note.getTitle()
                        + "\nСимволы: " + characters
                        + "\nСлова: " + words
                        + "\nСтроки: " + lines
        );
    }

    private void showMessage(AlertType type, String title, String text) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
