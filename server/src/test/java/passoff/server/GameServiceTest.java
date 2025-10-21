package passoff.server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.*;
import datamodel.*;
import service.*;

public class GameServiceTest {
    GameService gameService;
    GameAccess gameAccess;
    AuthAccess authAccess;

    @BeforeEach
    void setup() {
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        gameService = new GameService(gameAccess, authAccess);
        // Add a valid auth token for positive tests
        authAccess.addAuth(new RegisterResponse("user1", "token123"));
    }

    @Test
    void createGame_success() throws Exception {
        int gameId = gameService.createGame("token123", "ChessGame");
        assertTrue(gameId > 0);
    }

    @Test
    void createGame_invalidAuth_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.createGame("badtoken", "ChessGame"));
    }
}