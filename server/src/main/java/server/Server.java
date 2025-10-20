package server;

import com.google.gson.Gson;
import datamodel.*;
import io.javalin.*;
import io.javalin.http.Context;
import service.*;

import java.util.Map;

public class Server {

    private final Javalin server;
    private final UserService userService = new UserService();
    public Server() {

        server = Javalin.create(config -> config.staticFiles.add("web"));

        server.delete("db", ctx -> ctx.result("{}"));
        server.post("user", this::register);
    }

    private void register(Context ctx) {
        var serializer = new Gson();
        String reqJson = ctx.body();
        var user = serializer.fromJson(reqJson, UserData.class);
        // call service to register

        var registrationResponse = userService.register(user);


        ctx.result(serializer.toJson(registrationResponse));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
