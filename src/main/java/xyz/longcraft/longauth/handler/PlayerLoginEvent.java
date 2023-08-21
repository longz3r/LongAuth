package xyz.longcraft.longauth.handler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import xyz.longcraft.longauth.DatabaseManager;
import xyz.longcraft.longauth.LongAuth;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.bukkit.Bukkit.getLogger;

public class PlayerLoginEvent implements Listener {
    private final DatabaseManager databaseManager;
    private final FileConfiguration config;

    public PlayerLoginEvent(LongAuth plugin, DatabaseManager databaseManager, FileConfiguration config) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.databaseManager = databaseManager;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLoginEvent(org.bukkit.event.player.PlayerLoginEvent event) {
        InetAddress address = event.getAddress();
        String playerHostAddress = address.getHostAddress();
        String playerUsername = event.getPlayer().getName();

        try {
            ResultSet result = databaseManager.query(String.format("SELECT * FROM players WHERE username = '%s'", playerUsername));
            if (!result.next()) {
                getLogger().info(playerUsername + "is a new player, adding to database.");
                databaseManager.update(String.format("INSERT INTO `%s`.`players` (username, lastJoinedIP) VALUES ('%s', '%s')", config.getString("database_name"), playerUsername, playerHostAddress));
            } else {
                databaseManager.update(String.format("UPDATE players\n" +
                        "SET lastJoinedIP = '%s', lastJoinedDate = CURRENT_TIMESTAMP(), successJoinCount = successJoinCount + 1\n" +
                        "WHERE username = \"%s\";\n", playerHostAddress, playerUsername));
            }
        } catch (SQLException e) {
            getLogger().severe(e.getMessage());
        }
    }
}
