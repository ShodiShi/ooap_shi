package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:notes.db";

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public void initDatabase() {
        String createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
                """;

        String createNotesTable = """
                CREATE TABLE IF NOT EXISTS notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL DEFAULT '',
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    category_id INTEGER NOT NULL,
                    FOREIGN KEY (category_id) REFERENCES categories(id)
                        ON DELETE RESTRICT
                        ON UPDATE CASCADE
                )
                """;

        String createImportsTable = """
                CREATE TABLE IF NOT EXISTS imports (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    file_name TEXT NOT NULL,
                    source_type TEXT NOT NULL,
                    imported_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    note_id INTEGER NOT NULL,
                    FOREIGN KEY (note_id) REFERENCES notes(id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                )
                """;

        String createPluginsTable = """
                CREATE TABLE IF NOT EXISTS plugins (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    enabled INTEGER NOT NULL DEFAULT 1
                )
                """;

        String createPluginSettingsTable = """
                CREATE TABLE IF NOT EXISTS plugin_settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    plugin_id INTEGER NOT NULL,
                    setting_key TEXT NOT NULL,
                    setting_value TEXT,
                    UNIQUE (plugin_id, setting_key),
                    FOREIGN KEY (plugin_id) REFERENCES plugins(id)
                        ON DELETE CASCADE
                        ON UPDATE CASCADE
                )
                """;

        String insertDefaultPlugins = """
                INSERT OR IGNORE INTO plugins (name, enabled)
                VALUES
                    ('StatisticsPlugin', 1),
                    ('ExportTxtPlugin', 1),
                    ('ImportFilePlugin', 1),
                    ('ImportFolderPlugin', 1)
                """;

        String deleteRemovedPlugins = """
                DELETE FROM plugins
                WHERE name IN ('GenerateNotesPlugin', 'SpeechToTextPlugin')
                """;

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createCategoriesTable);
            statement.executeUpdate(createNotesTable);
            statement.executeUpdate(createImportsTable);
            statement.executeUpdate(createPluginsTable);
            migratePluginSettingsTable(statement);
            statement.executeUpdate(createPluginSettingsTable);
            statement.executeUpdate(deleteRemovedPlugins);
            statement.executeUpdate(insertDefaultPlugins);
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to initialize database", exception);
        }
    }

    private void migratePluginSettingsTable(Statement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery("PRAGMA table_info(plugin_settings)")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString("name");
                if ("plugin_name".equals(columnName)) {
                    statement.executeUpdate("DROP TABLE IF EXISTS plugin_settings");
                    break;
                }
            }
        }
    }
}
