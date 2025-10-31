// In server/src/main/java/dataaccess/MySQLUserAccess.java
package dataaccess;

import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class MySQLUserAccess implements UserAccess {

    private static final String CREATE_USER_TABLE = """
        CREATE TABLE IF NOT EXISTS user (
            `username` VARCHAR(255) NOT NULL,
            `password` VARCHAR(255) NOT NULL,
            `email` VARCHAR(255) NOT NULL,
            PRIMARY KEY (`username`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """;

    public MySQLUserAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(CREATE_USER_TABLE)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure user table", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM user WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                } else {
                    // Don't include SQLException as cause for "not found"
                    throw new DataAccessException("User not found: " + username);
                }
            }
        } catch (SQLException e) {
            // Include SQLException as cause for actual database errors
            throw new DataAccessException("Error finding user: " + e.getMessage(), e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.username());
            ps.setString(2, hashedPassword);
            ps.setString(3, user.email());

            ps.executeUpdate();

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new DataAccessException("User already exists: " + user.username());
            }
            throw new DataAccessException("Error creating user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        try {
            UserData user = getUser(username);
            return BCrypt.checkpw(password, user.password());
        } catch (DataAccessException e) {
            // If it's a database connection error, rethrow it
            if (e.getCause() instanceof SQLException) {
                throw e;
            }
            // Otherwise it's "user not found" - return false
            return false;
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM user";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing user table: " + e.getMessage(), e);
        }
    }
}