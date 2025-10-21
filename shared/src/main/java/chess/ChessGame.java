package chess;

import java.util.ArrayList;
import java.util.Collection;

import chess.ChessPiece.PieceType;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor turn;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        turn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return turn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.turn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            ChessBoard test = board.copy();
            test.addPiece(move.getStartPosition(), null);
            test.addPiece(move.getEndPosition(), piece);
            ChessGame testGame = new ChessGame();
            testGame.setBoard(test);
            if (!testGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        if (piece == null) {
            throw new InvalidMoveException();
        }
        if (piece.getTeamColor() != turn) {
            throw new InvalidMoveException();
        }
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException();
        }
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), piece);

        //add pawn promotion
        if (piece.getPieceType() == PieceType.PAWN && move.getPromotionPiece() != null) {
            board.addPiece(move.getEndPosition(), new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
        }

        turn = (turn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Finds the position of the king for a given team.
     * @param teamColor The team color of the king.
     * @return The ChessPosition of the king, or null if not found.
     */
    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition currentPos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(currentPos);
                if (piece != null && piece.getPieceType() == PieceType.KING && piece.getTeamColor() == teamColor) {
                    return currentPos;
                }
            }
        }
        return null; // Should not happen in a valid game
    }

    // --- NEW HELPER METHOD ---
    /**
     * Checks if a given position is attacked by any piece of the enemy team.
     * @param position The position to check.
     * @param enemyColor The color of the attacking team.
     * @return true if the position is under attack, false otherwise.
     */
    private boolean isAttackedByEnemy(ChessPosition position, TeamColor enemyColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition enemyPos = new ChessPosition(i, j);
                ChessPiece enemyPiece = board.getPiece(enemyPos);
                if (enemyPiece != null && enemyPiece.getTeamColor() == enemyColor) {
                    Collection<ChessMove> moves = enemyPiece.pieceMoves(board, enemyPos);
                    // Check if any move ends on the target position
                    if (moves != null) {
                        for (ChessMove move : moves) {
                            if (move.getEndPosition().equals(position)) {
                                return true; // Position is attacked
                            }
                        }
                    }
                }
            }
        }
        return false; // Position is safe
    }
    // --- END NEW HELPER METHOD ---


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = findKingPosition(teamColor);
        if (kingPosition == null) {
            return false; // King not on board, cannot be in check
        }

        TeamColor enemyColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        // Call the new helper method
        return isAttackedByEnemy(kingPosition, enemyColor); // <-- CHANGED
    }

    /**
     * Helper method to determine if a team has any valid (legal) moves.
     * @param teamColor The team to check.
     * @return true if the team has at least one valid move, false otherwise.
     */
    private boolean hasValidMoves(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++){
            for (int j = 1; j <= 8; j++) {
                ChessPosition currentPos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(currentPos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> validMoves = validMoves(currentPos);
                    if (validMoves != null && !validMoves.isEmpty()) {
                        return false; // Found a valid move
                    }
                }
            }
        }
        return true; // No valid moves found
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }
        return hasValidMoves(teamColor); // Is in check AND has no valid moves
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        return hasValidMoves(teamColor); // Is NOT in check AND has no valid moves
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame that = (ChessGame) o;
        return (this.turn == that.turn) &&
                (this.board != null ? this.board.equals(that.board) : that.board == null);
    }

    @Override
    public int hashCode() {
        int result = (board != null) ? board.hashCode() : 0;
        result = 31 * result + (turn != null ? turn.hashCode() : 0);
        return result;
    }
}