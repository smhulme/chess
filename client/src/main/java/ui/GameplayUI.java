package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import client.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameplayUI implements NotificationHandler {
    private final ServerFacade facade;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor clientColor;
    private final Scanner scanner = new Scanner(System.in);
    private WebSocketFacade wsFacade;
    private ChessGame currentGame;

    public GameplayUI(ServerFacade facade, String authToken, int gameID, ChessGame.TeamColor clientColor) {
        this.facade = facade;
        this.authToken = authToken;
        this.gameID = gameID;
        this.clientColor = clientColor;
    }

    public void run() {
        try {
            String wsUrl = facade.getServerUrl();
            wsFacade = new WebSocketFacade(wsUrl, this);
            wsFacade.connect(authToken, gameID);

            System.out.println("Entered Gameplay Mode. Type 'help' for commands.");
            boolean inGame = true;
            while (inGame) {
                System.out.print("[GAMEPLAY] >>> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

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
        } catch (Exception e) {
            System.out.println("Gameplay initialization error: " + e.getMessage());
        }
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

    private void handleLeave() throws Exception {
        wsFacade.leave(authToken, gameID);
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
        wsFacade.makeMove(authToken, gameID, move);
    }

    private void handleResign() throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String input = scanner.nextLine().trim().toLowerCase();
        if (input.equals("yes")) {
            wsFacade.resign(authToken, gameID);
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
        if (posStr.length() != 2) {
            return null;
        }
        char colChar = posStr.charAt(0);
        char rowChar = posStr.charAt(1);

        int col = colChar - 'a' + 1;
        int row = rowChar - '1' + 1;

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            return null;
        }

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
}
