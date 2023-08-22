package xyz.longcraft.longauth.handler;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.longcraft.longauth.DatabaseManager;
import xyz.longcraft.longauth.LongAuth;
import static org.bukkit.Bukkit.getLogger;

public class SQLog {

    public SQLog(String level, String message) {
        try {
            LongAuth longAuth = new LongAuth();
            DatabaseManager databaseManager = longAuth.getDatabaseManager();
            FileConfiguration config = longAuth.getFileConfiguration();
            databaseManager.update(String.format("INSERT INTO `%s`.`logs` (level, message) VALUES ('%s', '%s')", config.getString("database_name"), level, message));
        } catch (Exception e) {
            getLogger().info(e.getMessage());
        }

    }
}
