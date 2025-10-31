package server;

import dataaccess.DataAccessException;
import service.GameService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import java.util.HashSet;
import java.util.Map;
import datamodel.GameData;
import dataaccess.BadRequestException;
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
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public void createGame(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            record CreateGameRequest(String gameName) {}
            CreateGameRequest req = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
            String gameName = req.gameName();

            if (gameName == null || gameName.trim().isEmpty()) {
                ctx.status(400).json(Map.of("message", "Error: gameName is missing or empty"));
                return;
            }

            int gameID = gameService.createGame(authToken, gameName);
            ctx.status(200).json(Map.of("gameID", gameID));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(Map.of("message", "Error: malformed JSON"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }

    }

    public void joinGame(Context ctx) {
        if (!ctx.body().contains("\"gameID\":")) {
            ctx.status(400).json(Map.of("message", "Error: No gameID provided"));
            return;
        }
        String authToken = ctx.header("authorization");
        record JoinGameData(String playerColor, int gameID) {}
        JoinGameData joinData = new Gson().fromJson(ctx.body(), JoinGameData.class);
        try {
            boolean joinSuccess = gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());
            if (!joinSuccess) {
                ctx.status(403).json(Map.of("message", "Error: already taken"));
                return;
            }
            ctx.status(200).json(Map.of());
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

}
