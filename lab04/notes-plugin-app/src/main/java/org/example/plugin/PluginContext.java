package org.example.plugin;

import javafx.stage.Window;
import org.example.model.Note;
import org.example.repository.CategoryRepository;
import org.example.repository.NoteRepository;
import org.example.service.ImportService;

public class PluginContext {
    private final Note selectedNote;
    private final Window ownerWindow;
    private final ImportService importService;
    private final NoteRepository noteRepository;
    private final CategoryRepository categoryRepository;

    public PluginContext(
            Note selectedNote,
            Window ownerWindow,
            ImportService importService,
            NoteRepository noteRepository,
            CategoryRepository categoryRepository
    ) {
        this.selectedNote = selectedNote;
        this.ownerWindow = ownerWindow;
        this.importService = importService;
        this.noteRepository = noteRepository;
        this.categoryRepository = categoryRepository;
    }

    public Note getSelectedNote() {
        return selectedNote;
    }

    public Window getOwnerWindow() {
        return ownerWindow;
    }

    public ImportService getImportService() {
        return importService;
    }

    public NoteRepository getNoteRepository() {
        return noteRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }
}
