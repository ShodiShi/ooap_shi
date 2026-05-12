package org.example.plugin.impl.importing;

import java.nio.file.Path;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.DirectoryChooser;
import org.example.plugin.Plugin;
import org.example.plugin.PluginContext;

public class ImportFolderPlugin implements Plugin {
    @Override
    public String getName() {
        return "Импорт папки";
    }

    @Override
    public void execute(PluginContext context) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Импорт заметок из папки");

        var directory = directoryChooser.showDialog(context.getOwnerWindow());
        if (directory == null) {
            return;
        }

        try {
            int importedCount = context.getImportService().importFromFolder(Path.of(directory.toURI()));
            showMessage(AlertType.INFORMATION, "Импорт", "Импортировано файлов: " + importedCount);
        } catch (Exception exception) {
            showMessage(AlertType.ERROR, "Импорт", "Не удалось импортировать папку: " + exception.getMessage());
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
