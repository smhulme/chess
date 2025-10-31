package dataaccess;

import chess.ChessGame;
import datamodel.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

public class GameAccessTest {

    private GameAccess gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        // Initialize with MySQL implementation
        gameDAO = new MySQLGameAccess();
        // Clear the database before each test
        gameDAO.clear();
    }

    // --- createGame ---

    @Test
    public void createGamePositive() throws DataAccessException, BadRequestException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(1, null, null, "Test Game", game);
        gameDAO.createGame(gameData);

        // Verify game was created
        GameData fetchedGame = gameDAO.getGame(1);
        assertNotNull(fetchedGame);
        assertEquals(gameData.gameID(), fetchedGame.gameID());
        assertEquals(gameData.gameName(), fetchedGame.gameName());
        assertNotNull(fetchedGame.game());
    }

    @Test
    public void createGameNegative() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(1, null, null, "Game 1", game);
        gameDAO.createGame(gameData);

        // Try to create a game with the same ID
        GameData duplicate = new GameData(1, null, null, "Game 2", new ChessGame());
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(duplicate);
        }, "Should throw DataAccessException for duplicate game ID");
    }

    // --- getGame ---

    @Test
    public void getGamePositive() throws DataAccessException, BadRequestException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(42, "whitePlayer", "blackPlayer", "Epic Game", game);
        gameDAO.createGame(gameData);

        GameData foundGame = gameDAO.getGame(42);
        assertNotNull(foundGame);
        assertEquals(gameData.gameID(), foundGame.gameID());
        assertEquals(gameData.gameName(), foundGame.gameName());
        assertEquals(gameData.whiteUsername(), foundGame.whiteUsername());
        assertEquals(gameData.blackUsername(), foundGame.blackUsername());
    }

    @Test
    public void getGameNegative() {
        // Try to get a game that doesn't exist
        assertThrows(BadRequestException.class, () -> {
            gameDAO.getGame(9999);
        }, "Should throw BadRequestException when game not found");
    }

    // --- listGames ---

    @Test
    public void listGamesPositive() throws DataAccessException {
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();
        ChessGame game3 = new ChessGame();
        
        gameDAO.createGame(new GameData(1, "player1", "player2", "Game 1", game1));
        gameDAO.createGame(new GameData(2, "player3", "player4", "Game 2", game2));
        gameDAO.createGame(new GameData(3, null, null, "Game 3", game3));

        HashSet<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(3, games.size());
    }

    @Test
    public void listGamesNegative() throws DataAccessException {
        // List games when database is empty
        HashSet<GameData> games = gameDAO.listGames();
        assertNotNull(games);
        assertEquals(0, games.size(), "Empty database should return empty set");
    }

    // --- gameExists ---

    @Test
    public void gameExistsPositive() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(100, null, null, "Existing Game", game);
        gameDAO.createGame(gameData);

        boolean exists = gameDAO.gameExists(100);
        assertTrue(exists, "Game should exist");
    }

    @Test
    public void gameExistsNegative() throws DataAccessException {
        boolean exists = gameDAO.gameExists(9999);
        assertFalse(exists, "Non-existent game should return false");
    }

    // --- updateGame ---

    @Test
    public void updateGamePositive() throws DataAccessException, BadRequestException {
        ChessGame game = new ChessGame();
        GameData originalGame = new GameData(50, null, null, "Update Test", game);
        gameDAO.createGame(originalGame);

        // Update with players
        ChessGame updatedChessGame = new ChessGame();
        updatedChessGame.getBoard().resetBoard();
        GameData updatedGame = new GameData(50, "whitePlayer", "blackPlayer", "Update Test", updatedChessGame);
        gameDAO.updateGame(updatedGame);

        // Verify update
        GameData fetchedGame = gameDAO.getGame(50);
        assertEquals("whitePlayer", fetchedGame.whiteUsername());
        assertEquals("blackPlayer", fetchedGame.blackUsername());
        assertEquals("Update Test", fetchedGame.gameName());
    }

    @Test
    public void updateGameNegative() {
        // Try to update a game that doesn't exist
        ChessGame game = new ChessGame();
        GameData nonExistentGame = new GameData(9999, "player1", "player2", "Ghost Game", game);
        
        assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(nonExistentGame);
        }, "Should throw DataAccessException when updating non-existent game");
    }

    // --- clear ---

    @Test
    public void clearPositive() throws DataAccessException {
        ChessGame game1 = new ChessGame();
        ChessGame game2 = new ChessGame();
        
        gameDAO.createGame(new GameData(1, "p1", "p2", "Game 1", game1));
        gameDAO.createGame(new GameData(2, "p3", "p4", "Game 2", game2));

        gameDAO.clear();

        // Verify all games are gone
        HashSet<GameData> games = gameDAO.listGames();
        assertEquals(0, games.size(), "All games should be cleared");
        
        assertThrows(BadRequestException.class, () -> gameDAO.getGame(1));
        assertThrows(BadRequestException.class, () -> gameDAO.getGame(2));
    }
}