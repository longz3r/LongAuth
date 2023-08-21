package xyz.longcraft.longauth;

import java.sql.*;

import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {
    private Connection connection;

    public DatabaseManager(String host, String database, String username, String password) throws SQLException {
        String url = "jdbc:mysql://" + host + "/" + database;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            Logger logger = Bukkit.getLogger();
            logger.log(Level.SEVERE, "An error occurred: " + e.getMessage());
        }
    }

    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            Logger logger = Bukkit.getLogger();
            logger.log(Level.SEVERE, "An error occurred: " + e.getMessage());
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public ResultSet query(String query) {
        Connection conn = getConnection();
        try {
            Statement statement = conn.createStatement();
            return statement.executeQuery(query);
        } catch (SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
            return null;
        }
    }

    public void update(String query) {
        Connection conn =getConnection();
        try {
            Statement statement = conn.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            Bukkit.getLogger().severe((e.getMessage()));
        }
    }
}
