package ui;

import chess.ChessBoard;
import chess.ChessGame;
import client.ResponseException;
import client.ServerFacade;
import datamodel.GameData;
import datamodel.RegisterResponse;
import datamodel.UserData;

import java.util.*;

public class REPL {
    private final ServerFacade facade;
    private final Scanner scanner = new Scanner(System.in);
    private String authToken = null;
    private String username = null;
    private Map<Integer, GameData> gameNumberMap = new HashMap<>();

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
            System.out.print("Enter game number: ");
            String numStr = scanner.nextLine().trim();
            int gameNum = Integer.parseInt(numStr);
            
            if (!gameNumberMap.containsKey(gameNum)) {
                System.out.println("Invalid game number.");
                return;
            }
            
            System.out.print("Enter color (WHITE/BLACK): ");
            String color = scanner.nextLine().trim().toUpperCase();
            
            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Invalid color. Must be WHITE or BLACK.");
                return;
            }
            
            GameData game = gameNumberMap.get(gameNum);
            facade.joinGame(authToken, color, game.gameID());
            
            System.out.println("Joined game as " + color);
            
            // Draw board from appropriate perspective using your DrawBoard class
            ChessBoard board = new ChessBoard();
            board.resetBoard(); // Initialize to starting position
            ChessGame.TeamColor perspective = color.equals("WHITE") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            DrawBoard drawer = new DrawBoard(board);
            drawer.draw(perspective);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number format.");
        } catch (ResponseException e) {
            System.out.println("Failed to join game: " + getErrorMessage(e));
        }
    }

    private void handleObserveGame() {
        if (gameNumberMap.isEmpty()) {
            System.out.println("Please list games first using 'list' command.");
            return;
        }
        
        try {
            System.out.print("Enter game number: ");
            String numStr = scanner.nextLine().trim();
            int gameNum = Integer.parseInt(numStr);
            
            if (!gameNumberMap.containsKey(gameNum)) {
                System.out.println("Invalid game number.");
                return;
            }
            
            GameData game = gameNumberMap.get(gameNum);
            facade.joinGame(authToken, null, game.gameID());
            
            System.out.println("Observing game: " + game.gameName());
            
            // Observers see from white perspective
            ChessBoard board = new ChessBoard();
            board.resetBoard(); // Initialize to starting position
            DrawBoard drawer = new DrawBoard(board);
            drawer.draw(ChessGame.TeamColor.WHITE);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid game number format.");
        } catch (ResponseException e) {
            System.out.println("Failed to observe game: " + getErrorMessage(e));
        }
    }

    private String getErrorMessage(Exception e) {
        if (e instanceof ResponseException) {
            return e.getMessage();
        }
        return e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
    }
}