package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.db.DatabaseManager;
import org.example.model.ImportRecord;

public class ImportRepository {
    private final DatabaseManager databaseManager;

    public ImportRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void save(ImportRecord record) {
        String sql = """
                INSERT INTO imports(file_name, source_type, note_id)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, record.getFileName());
            statement.setString(2, record.getSourceType());
            statement.setInt(3, record.getNoteId());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    record.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to save import record", exception);
        }
    }

    public List<ImportRecord> findAll() {
        String sql = "SELECT id, file_name, source_type, imported_at, note_id FROM imports ORDER BY imported_at DESC";
        List<ImportRecord> records = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                records.add(new ImportRecord(
                        resultSet.getInt("id"),
                        resultSet.getString("file_name"),
                        resultSet.getString("source_type"),
                        resultSet.getString("imported_at"),
                        resultSet.getInt("note_id")
                ));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load import records", exception);
        }

        return records;
    }
}
