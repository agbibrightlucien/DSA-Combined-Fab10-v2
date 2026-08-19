package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection{
    private static final String URL = "jdbc:sqlite:campus_hub.db";
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Connection to campus_hub.db failed: " + e.getMessage());
        }
        return conn;
    }
}