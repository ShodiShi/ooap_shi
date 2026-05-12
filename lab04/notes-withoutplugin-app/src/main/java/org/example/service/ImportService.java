package org.example.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.example.model.Category;
import org.example.model.ImportRecord;
import org.example.model.Note;
import org.example.repository.CategoryRepository;
import org.example.repository.ImportRepository;
import org.example.repository.NoteRepository;

public class ImportService {
    private final NoteRepository noteRepository;
    private final ImportRepository importRepository;
    private final CategoryRepository categoryRepository;

    public ImportService(NoteRepository noteRepository, ImportRepository importRepository, CategoryRepository categoryRepository) {
        this.noteRepository = noteRepository;
        this.importRepository = importRepository;
        this.categoryRepository = categoryRepository;
    }

    public Note importFromFile(Path path) throws IOException {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            throw new IllegalStateException("No categories available for imported notes.");
        }

        String content = Files.readString(path, StandardCharsets.UTF_8);
        String fileName = path.getFileName().toString();
        String title = fileName.endsWith(".txt")
                ? fileName.substring(0, fileName.length() - 4)
                : fileName;

        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setCategoryId(categories.get(0).getId());
        noteRepository.save(note);

        ImportRecord record = new ImportRecord();
        record.setFileName(fileName);
        record.setSourceType("FILE");
        record.setNoteId(note.getId());
        importRepository.save(record);

        return note;
    }

    public int importFromFolder(Path folderPath) throws IOException {
        int importedCount = 0;

        try (Stream<Path> fileStream = Files.list(folderPath)) {
            List<Path> txtFiles = fileStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase().endsWith(".txt"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()))
                    .toList();

            for (Path txtFile : txtFiles) {
                importFromFile(txtFile);
                importedCount++;
            }
        }

        return importedCount;
    }
}
