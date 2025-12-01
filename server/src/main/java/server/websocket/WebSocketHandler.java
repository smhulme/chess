package server.websocket;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.ServerMessage;
import dataaccess.AuthAccess;
import dataaccess.GameAccess;
import datamodel.GameData;
import datamodel.RegisterResponse;
import chess.ChessGame;
import chess.ChessMove;

import java.io.IOException;
import java.util.Objects;

import java.time.Duration;

public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final AuthAccess authAccess;
    private final GameAccess gameAccess;

    public WebSocketHandler(AuthAccess authAccess, GameAccess gameAccess) {
        this.authAccess = authAccess;
        this.gameAccess = gameAccess;
    }

    public void register(WsConfig ws) {
        ws.onConnect(ctx -> {
            ctx.session.setIdleTimeout(Duration.ofMinutes(20));
        });
        ws.onMessage(this::onMessage);
    }

    private void onMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
            switch (command.getCommandType()) {
                case CONNECT -> connect(command.getAuthToken(), command.getGameID(), ctx);
                case MAKE_MOVE -> makeMove(ctx.message(), ctx);
                case LEAVE -> leave(command.getAuthToken(), command.getGameID(), ctx);
                case RESIGN -> resign(command.getAuthToken(), command.getGameID(), ctx);
            }
        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void connect(String authToken, Integer gameID, WsContext ctx) throws IOException {
        try {
            RegisterResponse authData = authAccess.getAuth(authToken);
            GameData gameData = gameAccess.getGame(gameID);

            if (gameData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Game not found")));
                return;
            }

            connections.add(authToken, gameID, ctx);

            String message;
            if (Objects.equals(authData.username(), gameData.whiteUsername())) {
                message = String.format("%s joined the game as WHITE", authData.username());
            } else if (Objects.equals(authData.username(), gameData.blackUsername())) {
                message = String.format("%s joined the game as BLACK", authData.username());
            } else {
                message = String.format("%s joined the game as an observer", authData.username());
            }

            ctx.send(new Gson().toJson(new ServerMessage.LoadGameMessage(gameData.game())));
            connections.broadcast(gameID, authToken, new ServerMessage.NotificationMessage(message));

        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void makeMove(String message, WsContext ctx) throws IOException {
        try {
            MakeMoveCommand command = new Gson().fromJson(message, MakeMoveCommand.class);
            String authToken = command.getAuthToken();
            Integer gameID = command.getGameID();
            ChessMove move = command.getMove();

            RegisterResponse authData = authAccess.getAuth(authToken);
            if (authData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Invalid auth token")));
                return;
            }

            GameData gameData = gameAccess.getGame(gameID);
            if (gameData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Game not found")));
                return;
            }

            ChessGame game = gameData.game();

            if (game.isGameOver()) {
                throw new Exception("Game is over");
            }

            if (game.getTeamTurn() == ChessGame.TeamColor.WHITE) {
                if (!Objects.equals(authData.username(), gameData.whiteUsername())) {
                    throw new Exception("Not your turn or not your piece");
                }
            } else {
                if (!Objects.equals(authData.username(), gameData.blackUsername())) {
                    throw new Exception("Not your turn or not your piece");
                }
            }

            game.makeMove(move);
            gameAccess.updateGame(gameData);

            connections.send(authToken, new ServerMessage.LoadGameMessage(game));
            connections.broadcast(gameID, authToken, new ServerMessage.LoadGameMessage(game));

            String moveMessage = String.format("%s made a move: %s", authData.username(), move.toString());
            connections.broadcast(gameID, authToken, new ServerMessage.NotificationMessage(moveMessage));

            String whiteUser = gameData.whiteUsername() != null ? gameData.whiteUsername() : "White";
            String blackUser = gameData.blackUsername() != null ? gameData.blackUsername() : "Black";

            if (game.isInCheckmate(ChessGame.TeamColor.WHITE)) {
                connections.broadcast(gameID, null,
                        new ServerMessage.NotificationMessage(String.format("%s is in CHECKMATE", whiteUser)));
                game.setGameOver(true);
                gameAccess.updateGame(gameData);
            } else if (game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(gameID, null,
                        new ServerMessage.NotificationMessage(String.format("%s is in CHECKMATE", blackUser)));
                game.setGameOver(true);
                gameAccess.updateGame(gameData);
            } else if (game.isInCheck(ChessGame.TeamColor.WHITE)) {
                connections.broadcast(gameID, null,
                        new ServerMessage.NotificationMessage(String.format("%s is in CHECK", whiteUser)));
            } else if (game.isInCheck(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(gameID, null,
                        new ServerMessage.NotificationMessage(String.format("%s is in CHECK", blackUser)));
            } else if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(gameID, null, new ServerMessage.NotificationMessage("Stalemate!"));
                game.setGameOver(true);
                gameAccess.updateGame(gameData);
            }

        } catch (chess.InvalidMoveException e) {
            ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Invalid move")));
        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void leave(String authToken, Integer gameID, WsContext ctx) throws IOException {
        try {
            RegisterResponse authData = authAccess.getAuth(authToken);
            if (authData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Invalid auth token")));
                return;
            }

            GameData gameData = gameAccess.getGame(gameID);
            if (gameData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Game not found")));
                return;
            }

            connections.remove(authToken);

            String message = String.format("%s left the game", authData.username());
            connections.broadcast(gameID, authToken, new ServerMessage.NotificationMessage(message));

            if (Objects.equals(authData.username(), gameData.whiteUsername())) {
                GameData updatedGame = new GameData(gameID, null, gameData.blackUsername(), gameData.gameName(),
                        gameData.game());
                gameAccess.updateGame(updatedGame);
            } else if (Objects.equals(authData.username(), gameData.blackUsername())) {
                GameData updatedGame = new GameData(gameID, gameData.whiteUsername(), null, gameData.gameName(),
                        gameData.game());
                gameAccess.updateGame(updatedGame);
            }

        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private void resign(String authToken, Integer gameID, WsContext ctx) throws IOException {
        try {
            RegisterResponse authData = authAccess.getAuth(authToken);
            if (authData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Invalid auth token")));
                return;
            }

            GameData gameData = gameAccess.getGame(gameID);
            if (gameData == null) {
                ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: Game not found")));
                return;
            }

            ChessGame game = gameData.game();

            if (game.isGameOver()) {
                throw new Exception("Game is already over");
            }

            if (!Objects.equals(authData.username(), gameData.whiteUsername())
                    && !Objects.equals(authData.username(), gameData.blackUsername())) {
                throw new Exception("Observers cannot resign");
            }

            game.setGameOver(true);
            gameAccess.updateGame(gameData);

            String message = String.format("%s resigned", authData.username());
            connections.broadcast(gameID, null, new ServerMessage.NotificationMessage(message));

        } catch (Exception e) {
            ctx.send(new Gson().toJson(new ServerMessage.ErrorMessage("Error: " + e.getMessage())));
        }
    }
}
