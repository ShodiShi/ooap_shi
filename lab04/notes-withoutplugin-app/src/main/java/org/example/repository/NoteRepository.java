package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.db.DatabaseManager;
import org.example.model.Note;

public class NoteRepository {
    private final DatabaseManager databaseManager;

    public NoteRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Note> findAll() {
        String sql = """
                SELECT id, title, content, created_at, updated_at, category_id
                FROM notes
                ORDER BY updated_at DESC, id DESC
                """;
        List<Note> notes = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                notes.add(mapRow(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load notes", exception);
        }

        return notes;
    }

    public Note findById(int id) {
        String sql = """
                SELECT id, title, content, created_at, updated_at, category_id
                FROM notes
                WHERE id = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load note by id", exception);
        }

        return null;
    }

    public void save(Note note) {
        if (note.getId() == 0) {
            insert(note);
        } else {
            update(note);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM notes WHERE id = ?";

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to delete note", exception);
        }
    }

    private void insert(Note note) {
        String sql = """
                INSERT INTO notes(title, content, category_id)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, note.getTitle());
            statement.setString(2, note.getContent());
            statement.setInt(3, note.getCategoryId());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    note.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to insert note", exception);
        }
    }

    private void update(Note note) {
        String sql = """
                UPDATE notes
                SET title = ?, content = ?, category_id = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, note.getTitle());
            statement.setString(2, note.getContent());
            statement.setInt(3, note.getCategoryId());
            statement.setInt(4, note.getId());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update note", exception);
        }
    }

    private Note mapRow(ResultSet resultSet) throws SQLException {
        return new Note(
                resultSet.getInt("id"),
                resultSet.getString("title"),
                resultSet.getString("content"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at"),
                resultSet.getInt("category_id")
        );
    }
}
