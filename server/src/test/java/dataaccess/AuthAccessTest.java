package dataaccess;

import datamodel.RegisterResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AuthAccessTest {

    private AuthAccess authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        // Initialize with MySQL implementation
        authDAO = new MySQLAuthAccess();
        // Clear the database before each test
        authDAO.clear();
    }

    // --- addAuth ---

    @Test
    public void addAuthPositive() throws DataAccessException {
        RegisterResponse authData = new RegisterResponse("testUser", "test-auth-token-123");
        authDAO.addAuth(authData);

        // Verify auth was added
        RegisterResponse fetchedAuth = authDAO.getAuth("test-auth-token-123");
        assertNotNull(fetchedAuth);
        assertEquals(authData.username(), fetchedAuth.username());
        assertEquals(authData.authToken(), fetchedAuth.authToken());
    }

    @Test
    public void addAuthNegative() throws DataAccessException {
        RegisterResponse authData = new RegisterResponse("user1", "duplicate-token");
        authDAO.addAuth(authData);

        // Try to add the same auth token again (should violate primary key constraint)
        RegisterResponse duplicate = new RegisterResponse("user2", "duplicate-token");
        assertThrows(DataAccessException.class, () -> {
            authDAO.addAuth(duplicate);
        }, "Should throw DataAccessException for duplicate auth token");
    }

    // --- getAuth ---

    @Test
    public void getAuthPositive() throws DataAccessException {
        RegisterResponse authData = new RegisterResponse("findMeUser", "find-me-token");
        authDAO.addAuth(authData);

        RegisterResponse foundAuth = authDAO.getAuth("find-me-token");
        assertNotNull(foundAuth);
        assertEquals(authData.username(), foundAuth.username());
        assertEquals(authData.authToken(), foundAuth.authToken());
    }

    @Test
    public void getAuthNegative() {
        // Try to get an auth token that doesn't exist
        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("nonExistentToken");
        }, "Should throw DataAccessException when auth token not found");
    }

    // --- deleteAuth ---

    @Test
    public void deleteAuthPositive() throws DataAccessException {
        RegisterResponse authData = new RegisterResponse("deleteUser", "delete-token");
        authDAO.addAuth(authData);

        // Verify it exists
        assertNotNull(authDAO.getAuth("delete-token"));

        // Delete it
        authDAO.deleteAuth("delete-token");

        // Verify it's gone
        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuth("delete-token");
        }, "Auth token should be deleted");
    }

    @Test
    public void deleteAuthNegative() throws DataAccessException {
        // Try to delete a non-existent auth token
        // This should not throw an exception, just do nothing
        assertDoesNotThrow(() -> {
            authDAO.deleteAuth("nonExistentToken");
        }, "Deleting non-existent auth token should not throw exception");

        // Verify that deleting doesn't affect other tokens
        RegisterResponse authData = new RegisterResponse("user1", "token1");
        authDAO.addAuth(authData);
        
        authDAO.deleteAuth("wrongToken");
        
        // Original token should still exist
        RegisterResponse foundAuth = authDAO.getAuth("token1");
        assertNotNull(foundAuth);
        assertEquals("user1", foundAuth.username());
    }

    // --- clear ---

    @Test
    public void clearPositive() throws DataAccessException {
        authDAO.addAuth(new RegisterResponse("user1", "token1"));
        authDAO.addAuth(new RegisterResponse("user2", "token2"));
        authDAO.addAuth(new RegisterResponse("user3", "token3"));

        authDAO.clear();

        // Verify all auth tokens are gone
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("token1"));
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("token2"));
        assertThrows(DataAccessException.class, () -> authDAO.getAuth("token3"));
    }
}