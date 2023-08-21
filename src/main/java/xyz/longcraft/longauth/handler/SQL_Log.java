package xyz.longcraft.longauth.handler;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.longcraft.longauth.DatabaseManager;

public class SQL_Log {
    public static void SQLlog(String level, String message, DatabaseManager databaseManager, FileConfiguration config) {
        databaseManager.update(String.format("INSERT INTO `%s`.`logs` (level, message) VALUES ('%s', '%s')", config.getString("database_name"), level, message));
    }
}
