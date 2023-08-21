package xyz.longcraft.longauth.handler;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import xyz.longcraft.longauth.DatabaseManager;
import xyz.longcraft.longauth.LongAuth;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class AsyncPlayerPreLoginEvent implements Listener {
    private final DatabaseManager databaseManager;
    private final FileConfiguration config;

    public AsyncPlayerPreLoginEvent(LongAuth plugin, DatabaseManager databaseManager, FileConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.databaseManager = databaseManager;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerPreLoginEvent(org.bukkit.event.player.AsyncPlayerPreLoginEvent event) {
        InetAddress address = event.getAddress();
        String playerHostAddress = address.getHostAddress();
        String playerUsername = event.getName();
        getLogger().info("Handling player " + playerUsername + ": " + playerHostAddress);

        try {
            if (this.databaseManager == null || !databaseManager.testConnection()) {
                event.disallow(org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Something went wrong with database, please contact server's owner immediately\n\nERR_CODE: DEO_BIET_01"));
                getLogger().severe("DATABASE CHET ROI");
                return;
            }
        } catch (Exception e) {
            getLogger().severe(e.getMessage());
        }

        try {
            ResultSet result = databaseManager.query(String.format("SELECT * FROM knownIP WHERE ip = \"%s\"", playerHostAddress));
            if (result == null) {
                event.disallow(org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Something went wrong with database, please contact server's owner immediately\n\nERR_CODE: DEO_BIET_02"));
                return;
            }
            if (result.next()) {
                String registeredUser = result.getString("username");
                getLogger().info("Registered user for this IP: " + registeredUser);
                if (playerUsername.equals(registeredUser)) {
                    event.allow();
                } else {
                    ResultSet whitelistedIP = databaseManager.query(String.format("SELECT * FROM whitelistedIP WHERE ip = \"%s\"", playerHostAddress));
                    if (whitelistedIP == null) {
                        event.disallow(org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(String.format("This IP was bound to %s\n%s\n\nERR_CODE: DUNG_ALT_AN_CUT", registeredUser, config.getString("ip_bound_message"))));
                        return;
                    }
                    if (whitelistedIP.next()) {
                        event.allow();
                        return;
                    }
                    event.disallow(org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(String.format("This IP was bound to %s\n%s\n\nERR_CODE: DUNG_ALT_AN_CUT", registeredUser, config.getString("ip_bound_message"))));
                }
            } else {
                getLogger().info(String.format("Allowing %s with new IP %s", playerUsername, playerHostAddress));
                event.allow();
                databaseManager.update(String.format("INSERT INTO `%s`.`knownIP` (`ip`, `username`) VALUES ('%s', '%s');",config.getString("database_name") ,playerHostAddress, playerUsername));
            }

        } catch (SQLException e) {
            getLogger().severe(e.getMessage());
        }
    }
}
