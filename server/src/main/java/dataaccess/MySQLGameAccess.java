package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.GameData;
import java.sql.*;
import java.util.HashSet;

public class MySQLGameAccess implements GameAccess {

    private final Gson gson = new Gson();

    private static final String CREATE_GAME_TABLE = """
        CREATE TABLE IF NOT EXISTS game (
            `gameID` INT NOT NULL AUTO_INCREMENT,
            `whiteUsername` VARCHAR(255) DEFAULT NULL,
            `blackUsername` VARCHAR(255) DEFAULT NULL,
            `gameName` VARCHAR(255) NOT NULL,
            `gameData` TEXT NOT NULL,
            PRIMARY KEY (`gameID`)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """;

    public MySQLGameAccess() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(CREATE_GAME_TABLE)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Unable to configure game table", ex);
        }
    }

    private String serializeGame(ChessGame game) {
        return gson.toJson(game);
    }

    private ChessGame deserializeGame(String gameData) {
        return gson.fromJson(gameData, ChessGame.class);
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        HashSet<GameData> games = new HashSet<>();
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, gameData FROM game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {
            while (rs.next()) {
                int gameID = rs.getInt("gameID");
                String whiteUsername = rs.getString("whiteUsername");
                String blackUsername = rs.getString("blackUsername");
                String gameName = rs.getString("gameName");
                String gameDataJson = rs.getString("gameData");
                ChessGame game = deserializeGame(gameDataJson);
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, game));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage(), e);
        }
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String gameDataJson = serializeGame(game.game());
        String sql = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, gameData) VALUES (?, ?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, game.gameID());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, game.gameName());
            ps.setString(5, gameDataJson);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage(), e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT gameID, whiteUsername, blackUsername, gameName, gameData FROM game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt("gameID"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            deserializeGame(rs.getString("gameData"))
                    );
                } else {
                    throw new DataAccessException("Game not found: " + gameID);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error finding game: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        String sql = "SELECT 1 FROM game WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error checking if game exists: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String gameDataJson = serializeGame(game.game());
        String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameData = ? WHERE gameID = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, gameDataJson);
            ps.setInt(4, game.gameID());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataAccessException("Game not found, could not update: " + game.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing game table: " + e.getMessage(), e);
        }
    }
}