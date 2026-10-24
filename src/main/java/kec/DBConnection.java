
package kec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            System.getenv().getOrDefault("KEC_DB_URL", "jdbc:mysql://localhost:3306/kec_academic");
    private static final String USER = System.getenv().getOrDefault("KEC_DB_USER", "root");

    public static Connection getConnection() throws SQLException {
        String password = System.getenv("KEC_DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new SQLException("KEC_DB_PASSWORD environment variable is required.");
        }
        return DriverManager.getConnection(URL, USER, password);
    }

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            System.out.println("MySQL database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
    }
}
