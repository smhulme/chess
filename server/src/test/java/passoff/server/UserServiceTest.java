package passoff.server;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.*;
import datamodel.*;
import service.UserService;

public class UserServiceTest {
    UserService userService;
    UserAccess userAccess;
    AuthAccess authAccess;

    @BeforeEach
    void setup() {
        userAccess = new MemoryUserAccess();
        authAccess = new MemoryAuthAccess();
        userService = new UserService(userAccess, authAccess);
    }

    @Test
    void register_success() throws Exception {
        UserData user = new UserData("user1", "pass", "email@example.com");
        RegisterResponse response = userService.register(user);
        assertNotNull(response);
        assertEquals("user1", response.username());
    }

    @Test
    void register_missingFields_throwsBadRequest() {
        UserData user = new UserData(null, "pass", "email@example.com");
        assertThrows(BadRequestException.class, () -> userService.register(user));
    }
}