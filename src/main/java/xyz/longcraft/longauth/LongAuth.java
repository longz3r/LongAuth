package xyz.longcraft.longauth;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.longcraft.longauth.handler.AsyncPlayerPreLoginEvent;
import xyz.longcraft.longauth.handler.PlayerLoginEvent;
import xyz.longcraft.longauth.handler.SQL_Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static xyz.longcraft.longauth.handler.SQL_Log.SQLlog;

public final class LongAuth extends JavaPlugin {
    private DatabaseManager databaseManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        String DB_HOST = config.getString("host");
        String DB_NAME = config.getString("database_name");
        String DB_USERNAME = config.getString("username");
        String DB_PASSWORD = config.getString("password");


        try {
            this.databaseManager = new DatabaseManager(DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD);
            ResultSet rs = databaseManager.query(String.format("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'knownIP';", DB_NAME));
            if (!rs.next()) {
                databaseManager.update("CREATE TABLE `knownIP` (\n" +
                        "\t`ip` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`username` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`firstJoinDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`ip`(15)) USING BTREE\n" +
                        ")\n" +
                        "COLLATE='utf8mb4_general_ci'\n" +
                        "ENGINE=InnoDB\n" +
                        ";\n");
            }

            rs = databaseManager.query(String.format("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'whitelistedIP';", DB_NAME));
            if (!rs.next()) {
                databaseManager.update("CREATE TABLE `whitelistedIP` (\n" +
                        "\t`ip` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci'\n" +
                        ")\n" +
                        "COLLATE='utf8mb4_general_ci'\n" +
                        "ENGINE=InnoDB\n" +
                        ";\n");

                databaseManager.update(String.format("INSERT INTO `%s`.`whitelistedIP` (`ip`) VALUES ('127.0.0.1');", DB_NAME));
            }

            rs = databaseManager.query(String.format("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'players';", DB_NAME));
            if (!rs.next()) {
                databaseManager.update("CREATE TABLE `players` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`username` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`lastJoinedIP` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`successJoinCount` INT(11) NOT NULL DEFAULT '0',\n" +
                        "\t`firstJoinDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\t`lastJoinedDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\tPRIMARY KEY (`id`, `username`(16)) USING BTREE\n" +
                        ")\n" +
                        "COLLATE='utf8mb4_general_ci'\n" +
                        "ENGINE=InnoDB\n" +
                        "AUTO_INCREMENT=4\n" +
                        ";\n");

            }

            rs = databaseManager.query(String.format("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = 'logs';", DB_NAME));
            if (!rs.next()) {
                databaseManager.update("CREATE TABLE `logs` (\n" +
                        "\t`id` INT(11) NOT NULL AUTO_INCREMENT,\n" +
                        "\t`date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
                        "\t`level` TINYTEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\t`message` TEXT NOT NULL COLLATE 'utf8mb4_general_ci',\n" +
                        "\tPRIMARY KEY (`id`) USING BTREE\n" +
                        ")\n" +
                        "COLLATE='utf8mb4_general_ci'\n" +
                        "ENGINE=InnoDB\n" +
                        ";");
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
            getLogger().severe(e.getMessage());
        }

        SQL_Log.SQLlog("INFO", "Plugin started", this.databaseManager, this.config);


        new AsyncPlayerPreLoginEvent(this, this.databaseManager, this.config);
        new PlayerLoginEvent(this, this.databaseManager, this.config);
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
        getLogger().info("PLUGIN CHET");
    }
}
