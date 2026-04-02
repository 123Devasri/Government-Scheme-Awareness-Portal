import java.sql.*;

public class DBHelper {

    // ── MongoDB via JDBC driver or MySQL ──
    // Change these to match your actual database
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/yojanaconnect";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "password";

    private static Connection connection = null;

    // Get single shared connection (Singleton)
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
                System.out.println("[DBHelper] Database connected successfully.");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DBHelper] JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DBHelper] Connection failed: " + e.getMessage());
        }
        return connection;
    }

    // Close the connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("[DBHelper] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DBHelper] Error closing connection: " + e.getMessage());
        }
    }

    // Test if DB is available (used by UI to show status)
    public static boolean isConnected() {
        try {
            return (connection != null && !connection.isClosed());
        } catch (SQLException e) {
            return false;
        }
    }
}
