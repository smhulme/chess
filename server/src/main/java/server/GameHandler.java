package server;

import service.GameService;
import io.javalin.http.Context;
import java.util.HashSet;
import java.util.Map;
import datamodel.GameData;

import dataaccess.UnauthorizedException;


public class GameHandler {

    GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            HashSet<GameData> games = gameService.listGames(authToken);
            ctx.status(200).json(Map.of("games", games));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: Unauthorized"));
        }
    }

    
}
