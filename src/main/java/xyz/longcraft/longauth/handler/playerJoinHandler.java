package xyz.longcraft.longauth.handler;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import xyz.longcraft.longauth.DatabaseManager;
import xyz.longcraft.longauth.LongAuth;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
public class playerJoinHandler implements Listener {
    private final DatabaseManager databaseManager;
    private final FileConfiguration config;

    public playerJoinHandler(LongAuth plugin, DatabaseManager databaseManager, FileConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.databaseManager = databaseManager;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        InetAddress address = event.getAddress();
        String playerHostAddress = address.getHostAddress();
        String playerUsername = event.getName();
        Bukkit.getLogger().info("Handling player " + playerUsername + ": " + playerHostAddress);

        try {
            if (this.databaseManager == null || !databaseManager.testConnection()) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Something went wrong with database, please contact server's owner immediately\n\nERR_CODE: DEO_BIET_01"));
                Bukkit.getLogger().severe("DATABASE CHET ROI");
                return;
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
        }

        try {
            ResultSet result = databaseManager.query(String.format("SELECT * FROM users WHERE ip = \"%s\"", playerHostAddress));
            if (result == null) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text("Something went wrong with database, please contact server's owner immediately\n\nERR_CODE: DEO_BIET_02"));
                return;
            }
            if (result.next()) {
                String registeredUser = result.getString("username");
                Bukkit.getLogger().info("Registered user for this IP: " + registeredUser);
                if (playerUsername.equals(registeredUser)) {
                    event.allow();
                } else {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(String.format("This IP was bound to %s\n%s\n\nERR_CODE: DUNG_ALT_AN_CUT", registeredUser, config.getString("ip_bound_message"))));
                }
            } else {
                Bukkit.getLogger().info(String.format("Allowing %s with new IP %s", playerUsername, playerHostAddress));
                event.allow();
                databaseManager.update(String.format("INSERT INTO `%s`.`users` (`ip`, `username`) VALUES ('%s', '%s');",config.getString("database_name") ,playerHostAddress, playerUsername));
            }

        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }
}
