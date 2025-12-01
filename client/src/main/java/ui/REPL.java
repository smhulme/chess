package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.InvalidMoveException;
import client.ResponseException;
import client.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import datamodel.GameData;
import datamodel.RegisterResponse;
import datamodel.UserData;
import websocket.messages.ServerMessage;

import java.util.*;

public class REPL implements NotificationHandler {
    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);
    private String authToken = null;
    private String username = null;
    private Map<Integer, GameData> gameNumberMap = new HashMap<>();

    private WebSocketFacade wsFacade;
    private ChessGame currentGame;
    private ChessGame.TeamColor clientColor;
    private int currentGameID;

    public REPL(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        System.out.println("♕ Welcome to 240 Chess Client. Type 'help' for commands.");
        boolean running = true;

        while (running) {
            try {
                if (authToken == null) {
                    running = runPrelogin();
                } else {
                    running = runPostlogin();
                }
            } catch (Exception e) {
                System.out.println("Error: " + getErrorMessage(e));
            }
        }

        System.out.println("Goodbye!");
    }

    private boolean runPrelogin() {
        System.out.print("[LOGGED OUT] >>> ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return true;
        }

        String[] tokens = input.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help":
                printPreloginHelp();
                break;
            case "quit":
            case "exit":
                return false;
            case "register":
                handleRegister();
                break;
            case "login":
                handleLogin();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }

        return true;
    }

    private boolean runPostlogin() {
        System.out.print("[" + username + "] >>> ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return true;
        }

        String[] tokens = input.split("\\s+");
        String cmd = tokens[0].toLowerCase();

        switch (cmd) {
            case "help":
                printPostloginHelp();
                break;
            case "logout":
                handleLogout();
                break;
            case "create":
                handleCreateGame();
                break;
            case "list":
                handleListGames();
                break;
            case "play":
                handlePlayGame();
                break;
            case "observe":
                handleObserveGame();
                break;
            case "quit":
            case "exit":
                return false;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }

        return true;
    }

    private void runGameplay() {
        System.out.println("Entered Gameplay Mode. Type 'help' for commands.");
        boolean inGame = true;
        while (inGame) {
            System.out.print("[GAMEPLAY] >>> ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty())
                continue;

            String[] tokens = input.split("\\s+");
            String cmd = tokens[0].toLowerCase();

            try {
                switch (cmd) {
                    case "help":
                        printGameplayHelp();
                        break;
                    case "redraw":
                        redrawBoard();
                        break;
                    case "leave":
                        handleLeave();
                        inGame = false;
                        break;
                    case "move":
                        handleMakeMove();
                        break;
                    case "resign":
                        handleResign();
                        break;
                    case "highlight":
                        handleHighlightMoves();
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printPreloginHelp() {
        System.out.println("Available commands:");
        System.out.println("  register - create a new account");
        System.out.println("  login - login to your account");
        System.out.println("  quit - exit the program");
        System.out.println("  help - show this help message");
    }

    private void printPostloginHelp() {
        System.out.println("Available commands:");
        System.out.println("  create - create a new game");
        System.out.println("  list - list all games");
        System.out.println("  play - join a game as a player");
        System.out.println("  observe - observe a game");
        System.out.println("  logout - logout of your account");
        System.out.println("  quit - exit the program");
        System.out.println("  help - show this help message");
    }

    private void printGameplayHelp() {
        System.out.println("Available commands:");
        System.out.println("  redraw - redraw the chess board");
        System.out.println("  leave - leave the game");
        System.out.println("  move - make a move");
        System.out.println("  resign - resign from the game");
        System.out.println("  highlight - highlight legal moves");
        System.out.println("  help - show this help message");
    }

    private void handleRegister() {
        try {
            System.out.print("Username: ");
            String user = scanner.nextLine().trim();

            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            if (user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
                System.out.println("All fields are required.");
                return;
            }

            UserData userData = new UserData(user, pass, email);
            RegisterResponse response = facade.register(userData);

            authToken = response.authToken();
            username = response.username();

            System.out.println("Successfully registered and logged in as " + username);
        } catch (ResponseException e) {
            System.out.println("Registration failed: " + getErrorMessage(e));
        }
    }

    private void handleLogin() {
        try {
            System.out.print("Username: ");
            String user = scanner.nextLine().trim();

            System.out.print("Password: ");
            String pass = scanner.nextLine().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                System.out.println("Username and password are required.");
                return;
            }

            UserData userData = new UserData(user, pass, null);
            RegisterResponse response = facade.login(userData);

            authToken = response.authToken();
            username = response.username();

            System.out.println("Successfully logged in as " + username);
        } catch (ResponseException e) {
            System.out.println("Login failed: " + getErrorMessage(e));
        }
    }

    private void handleLogout() {
        try {
            facade.logout(authToken);
            authToken = null;
            username = null;
            gameNumberMap.clear();
            System.out.println("Successfully logged out.");
        } catch (ResponseException e) {
            System.out.println("Logout failed: " + getErrorMessage(e));
        }
    }

    private void handleCreateGame() {
        try {
            System.out.print("Game name: ");
            String gameName = scanner.nextLine().trim();

            if (gameName.isEmpty()) {
                System.out.println("Game name is required.");
                return;
            }

            GameData gameData = new GameData(0, null, null, gameName, null);
            facade.createGame(gameData, authToken);

            System.out.println("Game created: " + gameName);
        } catch (ResponseException e) {
            System.out.println("Failed to create game: " + getErrorMessage(e));
        }
    }

    private void handleListGames() {
        try {
            ServerFacade.GameListResult result = facade.listGames(authToken);
            Collection<GameData> games = result.games();

            if (games == null || games.isEmpty()) {
                System.out.println("No games available.");
                return;
            }

            gameNumberMap.clear();
            int number = 1;

            System.out.println("\nAvailable Games:");
            for (GameData game : games) {
                gameNumberMap.put(number, game);

                String whitePlayer = game.whiteUsername() != null ? game.whiteUsername() : "(empty)";
                String blackPlayer = game.blackUsername() != null ? game.blackUsername() : "(empty)";

                System.out.printf("%d. %s - White: %s, Black: %s%n",
                        number, game.gameName(), whitePlayer, blackPlayer);
                number++;
            }
            System.out.println();
        } catch (ResponseException e) {
            System.out.println("Failed to list games: " + getErrorMessage(e));
        }
    }

    private void handlePlayGame() {
        if (gameNumberMap.isEmpty()) {
            System.out.println("Please list games first using 'list' command.");
            return;
        }

        try {
            Integer gameNum = getValidGameNumber();
            if (gameNum == null) {
                return;
            }

            System.out.print("Enter color (WHITE/BLACK): ");
            String colorStr = scanner.nextLine().trim().toUpperCase();

            if (!colorStr.equals("WHITE") && !colorStr.equals("BLACK")) {
                System.out.println("Invalid color. Must be WHITE or BLACK.");
                return;
            }

            GameData game = gameNumberMap.get(gameNum);
            facade.joinGame(authToken, colorStr, game.gameID());

            this.currentGameID = game.gameID();
            this.clientColor = colorStr.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            // Initialize WebSocket
            String wsUrl = facade.getServerUrl();
            wsFacade = new WebSocketFacade(wsUrl, this);
            wsFacade.connect(authToken, currentGameID);

            runGameplay();

        } catch (ResponseException e) {
            System.out.println("Failed to join game: " + getErrorMessage(e));
        } catch (Exception e) {
            System.out.println("An error occurred: " + getErrorMessage(e));
        }
    }

    private void handleObserveGame() {
        if (gameNumberMap.isEmpty()) {
            System.out.println("Please list games first using 'list' command.");
            return;
        }

        try {
            Integer gameNum = getValidGameNumber();
            if (gameNum == null) {
                return;
            }

            GameData game = gameNumberMap.get(gameNum);

            this.currentGameID = game.gameID();
            this.clientColor = ChessGame.TeamColor.WHITE; // Observer defaults to White perspective (or maybe ask?)
            // Initialize WebSocket
            String wsUrl = facade.getServerUrl();
            wsFacade = new WebSocketFacade(wsUrl, this);
            wsFacade.connect(authToken, currentGameID);

            runGameplay();

        } catch (ResponseException e) {
            System.out.println("Failed to observe game: " + getErrorMessage(e));
        } catch (Exception e) {
            System.out.println("An error occurred: " + getErrorMessage(e));
        }
    }

    private void handleLeave() throws Exception {
        wsFacade.leave(authToken, currentGameID);

        wsFacade = null;
        currentGame = null;
    }

    private void handleMakeMove() throws Exception {
        if (currentGame == null) {
            System.out.println("Game not loaded yet.");
            return;
        }

        System.out.print("Enter start position (e.g. e2): ");
        String startStr = scanner.nextLine().trim();
        System.out.print("Enter end position (e.g. e4): ");
        String endStr = scanner.nextLine().trim();

        // Parse positions
        ChessPosition start = parsePosition(startStr);
        ChessPosition end = parsePosition(endStr);

        if (start == null || end == null) {
            System.out.println("Invalid position format.");
            return;
        }

        // Check for promotion
        ChessPiece.PieceType promotionPiece = null;
        // Simple check: if pawn moving to last rank
        ChessPiece piece = currentGame.getBoard().getPiece(start);
        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && end.getRow() == 8) ||
                    (piece.getTeamColor() == ChessGame.TeamColor.BLACK && end.getRow() == 1)) {
                System.out.print("Enter promotion piece (QUEEN/ROOK/BISHOP/KNIGHT): ");
                String promoStr = scanner.nextLine().trim().toUpperCase();
                try {
                    promotionPiece = ChessPiece.PieceType.valueOf(promoStr);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid promotion piece. Defaulting to QUEEN.");
                    promotionPiece = ChessPiece.PieceType.QUEEN;
                }
            }
        }

        ChessMove move = new ChessMove(start, end, promotionPiece);
        wsFacade.makeMove(authToken, currentGameID, move);
    }

    private void handleResign() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.equals("yes")) {
            wsFacade.resign(authToken, currentGameID);
        }
    }

    private void handleHighlightMoves() {
        if (currentGame == null) {
            System.out.println("Game not loaded yet.");
            return;
        }

        System.out.print("Enter piece position to highlight (e.g. e2): ");
        String posStr = scanner.nextLine().trim();
        ChessPosition pos = parsePosition(posStr);

        if (pos == null) {
            System.out.println("Invalid position format.");
            return;
        }

        // Use DrawBoard to highlight
        DrawBoard drawer = new DrawBoard(currentGame.getBoard());
        Collection<ChessMove> validMoves = currentGame.validMoves(pos);
        if (validMoves == null) {
            System.out.println("No piece at that position.");
            return;
        }

        drawer.draw(clientColor, pos, validMoves);
    }

    private void redrawBoard() {
        if (currentGame != null) {
            DrawBoard drawer = new DrawBoard(currentGame.getBoard());
            drawer.draw(clientColor);
        } else {
            System.out.println("No game state available.");
        }
    }

    private ChessPosition parsePosition(String posStr) {
        if (posStr.length() != 2)
            return null;
        char colChar = posStr.charAt(0);
        char rowChar = posStr.charAt(1);

        int col = colChar - 'a' + 1;
        int row = rowChar - '1' + 1;

        if (col < 1 || col > 8 || row < 1 || row > 8)
            return null;

        return new ChessPosition(row, col);
    }

    // NotificationHandler implementation
    @Override
    public void notify(ServerMessage message) {
        switch (message.getServerMessageType()) {
            case LOAD_GAME:
                ServerMessage.LoadGameMessage loadMsg = (ServerMessage.LoadGameMessage) message;
                this.currentGame = loadMsg.getGame();
                redrawBoard();
                System.out.print("[GAMEPLAY] >>> "); // Reprint prompt
                break;
            case ERROR:
                ServerMessage.ErrorMessage errorMsg = (ServerMessage.ErrorMessage) message;
                System.out.println("\n" + errorMsg.getErrorMessage());
                System.out.print("[GAMEPLAY] >>> ");
                break;
            case NOTIFICATION:
                ServerMessage.NotificationMessage notifMsg = (ServerMessage.NotificationMessage) message;
                System.out.println("\n" + notifMsg.getMessage());
                System.out.print("[GAMEPLAY] >>> ");
                break;
        }
    }

    private Integer getValidGameNumber() {
        System.out.print("Enter game number: ");
        String numStr = scanner.nextLine().trim();

        int gameNum;
        try {
            gameNum = Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid numerical game number.");
            return null;
        }

        if (gameNum < 1 || gameNum > gameNumberMap.size()) {
            System.out.println("Invalid game number. Please enter a number between 1 and " + gameNumberMap.size());
            return null;
        }

        if (!gameNumberMap.containsKey(gameNum)) {
            System.out.println("Invalid game number.");
            return null;
        }

        return gameNum;
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof ResponseException) {
            ResponseException re = (ResponseException) e;
                        // Return only the message, not the status code
            return re.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
    }
}