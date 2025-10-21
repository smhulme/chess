package server;

import com.google.gson.Gson;

import dataaccess.UserAccess;
import dataaccess.MemoryUserAccess;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin server;
    UserAccess dataAccess = new MemoryUserAccess();
    private final UserService userService = new UserService(dataAccess);
    public Server() {

        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        try {
            String reqJson = ctx.body();
            var user = serializer.fromJson(reqJson, UserData.class);
            
            var registrationResponse = userService.register(user);
            
            ctx.result(serializer.toJson(registrationResponse));
        } catch (Exception e) {
            // Handle different types of registration errors
            ctx.status(400); // Bad Request
            ctx.result(serializer.toJson(Map.of("message", "Registration failed: " + e.getMessage())));
        }
    }

    public int run(int desiredPort) throws Exception {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
