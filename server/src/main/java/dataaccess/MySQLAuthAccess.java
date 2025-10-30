// In server/src/main/java/dataaccess/MySQLAuthAccess.java
package dataaccess;

import datamodel.RegisterResponse;
import java.sql.*;
import java.util.UUID;

public class MySQLAuthAccess implements AuthAccess {

    private static final String CREATE_AUTH_TABLE = """
        CREATE TABLE IF NOT EXISTS auth (
            `authToken` VARCHAR(255) NOT NULL,
            `username` VARCHAR(255) NOT NULL,
            PRIMARY KEY (`authToken`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """;

    public MySQLAuthAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(CREATE_AUTH_TABLE)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure auth table", ex);
        }
    }

    @Override
    public void addAuth(RegisterResponse authData) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error adding auth token: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error deleting auth token: " + e.getMessage(), e);
        }
    }

    @Override
    public RegisterResponse getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT authToken, username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new RegisterResponse(
                            rs.getString("username"),
                            rs.getString("authToken")
                    );
                } else {
                    throw new DataAccessException("Auth token not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding auth token: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing auth table: " + e.getMessage(), e);
        }
    }
}