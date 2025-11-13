package ui;

import client.ResponseException;
import client.ServerFacade;
import datamodel.GameData;
import datamodel.UserData;
import chess.ChessGame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChessClient {
    // 1. Define the State enum as a public inner class/enum
    public enum State {
        SIGNED_OUT,
        SIGNED_IN
    }

    private final ServerFacade server;
    private final String serverUrl;
    // 2. The type reference remains the same: State
    private State state = State.SIGNED_OUT;
    private String authToken = null;
    // Map to convert the visual number (1, 2, 3) to the actual Game ID
    private Map<Integer, Integer> gameIdMap = new HashMap<>();

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "login" -> login(params);
                case "register" -> register(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
//                case "list" -> listGames();
//                case "join" -> joinGame(params);
//                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws ResponseException {
        if (params.length >= 2) {
            var userData = new UserData(params[0], params[1], null);
            var authData = server.login(userData);
            authToken = authData.authToken();
            state = State.SIGNED_IN;
            return String.format("You logged in as %s.", authData.username());
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String register(String... params) throws ResponseException {
        if (params.length >= 3) {
            var userData = new UserData(params[0], params[1], params[2]);
            var authData = server.register(userData);
            authToken = authData.authToken();
            state = State.SIGNED_IN;
            return String.format("You logged in as %s.", authData.username());
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String logout() throws ResponseException {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        state = State.SIGNED_OUT;
        return "Logged out.";
    }

    public String createGame(String... params) throws ResponseException {
        assertSignedIn();
        if (params.length >= 1) {
            var gameName = params[0];
            // Using a dummy GameData for the request
            var gameData = new GameData(0, null, null, gameName, null);
            gameData = server.createGame(gameData, authToken);
            return String.format("Created game: %s with ID: %d", gameData.gameName(), gameData.gameID());
        }
        throw new ResponseException(400, "Expected: <NAME>");
    }


    public String help() {
        if (state == State.SIGNED_OUT) {
            return """
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    - help
                    """;
        } else {
            return """
                    - create <NAME>
                    - list
                    - join <ID> [WHITE|BLACK]
                    - observe <ID>
                    - logout
                    - quit
                    - help
                    """;
        }
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.SIGNED_OUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}