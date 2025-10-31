package dataaccess;

import datamodel.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserAccessTest {

    private UserAccess userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        // Initialize with MySQL implementation
        userDAO = new MySQLUserAccess();
        // Clear the database before each test
        userDAO.clear();
    }

    // --- createUser ---

    @Test
    public void createUserPositive() throws DataAccessException {
        UserData user = new UserData("testUser", "testPass", "test@email.com");
        userDAO.createUser(user);

        // Verify user was created
        UserData fetchedUser = userDAO.getUser("testUser");
        assertNotNull(fetchedUser);
        assertEquals(user.username(), fetchedUser.username());
        assertEquals(user.email(), fetchedUser.email());
        // Note: We don't check the password directly, but authenticateUser will
    }

    @Test
    public void createUserNegative() throws DataAccessException {
        UserData user = new UserData("duplicateUser", "pass1", "email1@com");
        userDAO.createUser(user);

        // Try to create the same user again
        UserData duplicate = new UserData("duplicateUser", "pass2", "email2@com");
        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(duplicate);
        }, "Should throw DataAccessException for duplicate user");
    }

    // --- getUser ---

    @Test
    public void getUserPositive() throws DataAccessException {
        UserData user = new UserData("findMe", "pass", "find@me.com");
        userDAO.createUser(user);

        UserData foundUser = userDAO.getUser("findMe");
        assertNotNull(foundUser);
        assertEquals(user.username(), foundUser.username());
    }

    @Test
    public void getUserNegative() {
        // Try to get a user that doesn't exist
        assertThrows(DataAccessException.class, () -> {
            userDAO.getUser("nonExistentUser");
        }, "Should throw DataAccessException when user not found");
    }

    // --- authenticateUser ---

    @Test
    public void authenticateUserPositive() throws DataAccessException {
        UserData user = new UserData("authUser", "correctPassword", "auth@user.com");
        userDAO.createUser(user); // createUser handles the hashing

        boolean isAuthenticated = userDAO.authenticateUser("authUser", "correctPassword");
        assertTrue(isAuthenticated, "User should be authenticated with correct password");
    }

    @Test
    public void authenticateUserNegative() throws DataAccessException {
        UserData user = new UserData("authUser", "correctPassword", "auth@user.com");
        userDAO.createUser(user);

        // Try with wrong password
        boolean isAuthenticatedWrongPass = userDAO.authenticateUser("authUser", "wrongPassword");
        assertFalse(isAuthenticatedWrongPass, "User should not be authenticated with wrong password");

        // Try with non-existent user
        boolean isAuthenticatedNonExistent = userDAO.authenticateUser("nonExistentUser", "anyPassword");
        assertFalse(isAuthenticatedNonExistent, "Non-existent user should not be authenticated");
    }

    // --- clear ---

    @Test
    public void clearPositive() throws DataAccessException {
        userDAO.createUser(new UserData("user1", "pass1", "e1@com"));
        userDAO.createUser(new UserData("user2", "pass2", "e2@com"));

        userDAO.clear();

        // Verify users are gone
        assertThrows(DataAccessException.class, () -> userDAO.getUser("user1"));
        assertThrows(DataAccessException.class, () -> userDAO.getUser("user2"));
    }
}