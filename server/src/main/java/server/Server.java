package server;

import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;
import com.google.gson.Gson;
import io.javalin.json.JavalinGson;

public class Server {

    UserAccess userAccess;
    AuthAccess authAccess;
    GameAccess gameAccess;
    UserService userService;
    GameService gameService;
    UserHandler userHandler;
    GameHandler gameHandler;

    private Javalin server;

    public Server() {

        userAccess = new MemoryUserAccess();
        authAccess = new MemoryAuthAccess();
        userAccess = new MemoryUserAccess();
        authAccess = new MemoryAuthAccess();
        gameAccess = new MemoryGameAccess();
        userService = new UserService(userAccess, authAccess);
        gameService = new GameService(gameAccess, authAccess);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);

    }

    public int run(int desiredPort) {
        server = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson()); // <-- Add this line here
        }).start(desiredPort);

        server.delete("/db", this::clear);
        server.post("/user", userHandler::register);
        server.post("/session", userHandler::login);
        server.delete("/session", userHandler::logout);

        server.get("/game", gameHandler::listGames);
        server.post("/game", gameHandler::createGame);
        server.put("/game", gameHandler::joinGame);

        server.exception(UnauthorizedException.class, (e, ctx) -> ctx.status(401).json(new ErrorResponse("Error: unauthorized")));
        server.exception(BadRequestException.class, (e, ctx) -> ctx.status(400).json(new ErrorResponse("Error: bad request")));
        server.exception(Exception.class, (e, ctx) -> ctx.status(500).json(new ErrorResponse("Internal server error")));

        return server.port();
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private void clear(Context ctx) {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200).json("{}");
        } catch (DataAccessException e) {
            ctx.status(500).json(new ErrorResponse("Internal server error: " + e.getMessage()));
        }
    }

    private static class ErrorResponse {
        public final String message;
        public ErrorResponse(String message) { 
            this.message = message; 
        }
    }
}
