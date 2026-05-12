package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.example.db.DatabaseManager;
import org.example.model.Category;

public class CategoryRepository {
    private final DatabaseManager databaseManager;

    public CategoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Category> findAll() {
        String sql = "SELECT id, name FROM categories ORDER BY name";
        List<Category> categories = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(new Category(
                        resultSet.getInt("id"),
                        resultSet.getString("name")
                ));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load categories", exception);
        }

        return categories;
    }

    public void insertDefaultCategories() {
        String sql = "INSERT OR IGNORE INTO categories(name) VALUES (?)";
        String[] defaultCategories = {"Идеи", "Учеба", "Личное"};

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String category : defaultCategories) {
                statement.setString(1, category);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to insert default categories", exception);
        }
    }
}
