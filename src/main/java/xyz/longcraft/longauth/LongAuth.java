package xyz.longcraft.longauth;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.longcraft.longauth.handler.playerJoinHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class LongAuth extends JavaPlugin {
    private DatabaseManager databaseManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("NGU VAI");


        saveDefaultConfig();
        config = getConfig();
        String DB_HOST = config.getString("host");
        String DB_NAME = config.getString("database_name");
        String DB_USERNAME = config.getString("username");
        String DB_PASSWORD = config.getString("password");


        try {
            this.databaseManager = new DatabaseManager(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            ResultSet rs1 = databaseManager.query(String.format("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'users';", DB_NAME));
            if (!rs1.next()) {
                databaseManager.update("CREATE TABLE `users` (\n" +
                        "\t`ip` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`username` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`firstJoinDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`ip`(15)) USING BTREE\n" +
                        ")\n" +
                        "COLLATE='utf8mb4_general_ci'\n" +
                        "ENGINE=InnoDB\n" +
                        ";\n");
            }
        } catch (SQLException e) {
            getLogger().severe("Failed to connect to the database.");
            Logger logger = Bukkit.getLogger();
            logger.log(Level.SEVERE, "An error occurred: " + e.getMessage());
        }

        try {
            boolean isDatabaseWorking = this.databaseManager.testConnection();
            if (isDatabaseWorking) {
                getLogger().info("Database is working!");
            } else {
                getLogger().severe("Database is not working!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }


        new playerJoinHandler(this, this.databaseManager, this.config);
    }

    @Override
    public void onDisable() {
        try {
            if (databaseManager != null) {
                databaseManager.closeConnection();
            }
        } catch (SQLException e) {
            Logger logger = Bukkit.getLogger();
            logger.log(Level.SEVERE, "An error occurred: " + e.getMessage());
        }
        // Plugin shutdown logic
        Bukkit.getLogger().info("PLUGIN CHET");
    }
}
