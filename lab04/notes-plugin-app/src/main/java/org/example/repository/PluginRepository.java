package org.example.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.db.DatabaseManager;

public class PluginRepository {
    private final DatabaseManager databaseManager;

    public PluginRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<String> findEnabledPluginNames() {
        String sql = """
                SELECT name
                FROM plugins
                WHERE enabled = 1
                ORDER BY name
                """;
        List<String> pluginNames = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                pluginNames.add(resultSet.getString("name"));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load enabled plugins", exception);
        }

        return pluginNames;
    }

    public Map<String, Boolean> findPluginStates() {
        String sql = """
                SELECT name, enabled
                FROM plugins
                ORDER BY name
                """;
        Map<String, Boolean> pluginStates = new HashMap<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                pluginStates.put(
                        resultSet.getString("name"),
                        resultSet.getInt("enabled") == 1
                );
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to load plugin states", exception);
        }

        return pluginStates;
    }

    public void updatePluginEnabled(String name, boolean enabled) {
        String sql = """
                UPDATE plugins
                SET enabled = ?
                WHERE name = ?
                """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, enabled ? 1 : 0);
            statement.setString(2, name);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update plugin state", exception);
        }
    }
}
