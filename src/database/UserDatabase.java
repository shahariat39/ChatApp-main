package src.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

public class UserDatabase {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3308/chatapp";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    public static void main(String[] args) {
        try {
            // Establish connection
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);

            // Create statement
            Statement statement = connection.createStatement();

            // Execute query to retrieve all users
            String query = "SELECT username FROM users";
            ResultSet resultSet = statement.executeQuery(query);

            // Process the results
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                System.out.println("Username: " + username);
                // Add username to your GUI user list
                // userListModel.addElement(username);
            }

            // Close resources
            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

  
}
