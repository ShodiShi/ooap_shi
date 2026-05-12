package org.example.model;

public class ImportRecord {
    private int id;
    private String fileName;
    private String sourceType;
    private String importedAt;
    private int noteId;

    public ImportRecord() {
    }

    public ImportRecord(int id, String fileName, String sourceType, String importedAt, int noteId) {
        this.id = id;
        this.fileName = fileName;
        this.sourceType = sourceType;
        this.importedAt = importedAt;
        this.noteId = noteId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getImportedAt() {
        return importedAt;
    }

    public void setImportedAt(String importedAt) {
        this.importedAt = importedAt;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }
}
