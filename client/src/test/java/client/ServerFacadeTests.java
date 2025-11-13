package client;

import datamodel.RegisterResponse;
import datamodel.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static String serverUrl;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        int port = server.run(0);
        serverUrl = "http://localhost:" + port;
        facade = new ServerFacade(serverUrl);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException {
        facade.clear(); 
    }

    @Test
    public void testRegisterAndLogin() throws Exception {
        UserData user = new UserData("testuser", "testpass", "test@byu.edu");
        RegisterResponse regResp = facade.register(user);
        assertNotNull(regResp);
        assertEquals("testuser", regResp.username());
        assertNotNull(regResp.authToken());

        // Now logout and login again
        facade.logout(regResp.authToken());
        RegisterResponse loginResp = facade.login(new UserData("testuser", "testpass", null));
        assertNotNull(loginResp);
        assertEquals("testuser", loginResp.username());
        assertNotNull(loginResp.authToken());
    }

    @Test
    public void testRegisterMissingFields() {
        UserData badUser = new UserData(null, "pass", "email");
        assertThrows(ResponseException.class, () -> facade.register(badUser));
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        UserData user = new UserData("user2", "pass2", "user2@byu.edu");
        facade.register(user);
        assertThrows(ResponseException.class, () -> facade.login(new UserData("user2", "wrongpass", null)));
    }

    @Test
    public void testLogout() throws Exception {
        UserData user = new UserData("user3", "pass3", "user3@byu.edu");
        RegisterResponse regResp = facade.register(user);
        assertDoesNotThrow(() -> facade.logout(regResp.authToken()));
    }

    @Test
    public void testDuplicateRegister() throws Exception {
        UserData user = new UserData("dupe", "pass", "dupe@byu.edu");
        facade.register(user);
        assertThrows(ResponseException.class, () -> facade.register(user));
    }
}
