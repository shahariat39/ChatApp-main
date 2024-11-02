package src.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class UserAuthentication {
    private static final String DB_URL = "jdbc:mysql://localhost:3308/chatapp";
    private static final String DB_USER = "root"; // replace with your DB username
    private static final String DB_PASSWORD = ""; // replace with your DB password

    public static boolean authenticateUser(String username, String password) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean isAuthenticated = resultSet.next();
            connection.close();
            return isAuthenticated;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Set<String> getAllUsers() {
        Set<String> users = new HashSet<>();
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT username FROM users";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                users.add(resultSet.getString("username"));
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
}
