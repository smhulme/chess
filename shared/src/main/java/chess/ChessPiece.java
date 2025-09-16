package chess;

import java.util.Collection;
import java.util.HashSet;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */


    private Collection<ChessMove> getLinearMoves(ChessBoard board, ChessPosition myPosition, int[][] directions) {
        Collection<ChessMove> moves = new HashSet<>();
        for (int[] dir : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while(true){
                row += dir[0];
                col += dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) break;
                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece occupyingPiece = board.getPiece(newPosition);
                if (occupyingPiece == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else {
                    if (occupyingPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        Collection<ChessMove> moves = new HashSet<>();

        ChessPiece piece = board.getPiece(myPosition);

        if (piece.getPieceType() == PieceType.BISHOP) {
            int [][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1}
            };
            moves.addAll(getLinearMoves(board, myPosition, directions));
        }

        if (piece.getPieceType() == PieceType.ROOK) {
            int [][] directions = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1}
            };
            moves.addAll(getLinearMoves(board, myPosition, directions));
        }

        if (piece.getPieceType() == PieceType.QUEEN) {
            int[][] directions = {
                {1, 1},
                {1, -1},
                {-1, 1},
                {-1, -1},
                {1,0},
                {0,1},
                {-1,0},
                {0,-1}
            };
            moves.addAll(getLinearMoves(board, myPosition, directions));
        }

        if (piece.getPieceType() == PieceType.KNIGHT) {
            int[][] directions = {
                {1, 2},
                {2, 1},
                {-1, 2},
                {-2, 1},
                {-1, -2},
                {-2, -1},
                {1, -2},
                {2, -1}
            };

            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                } else {
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPosition);
                    if (occupyingPiece == null || occupyingPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
                
            }
        }

        if (piece.getPieceType() == PieceType.KING) {
            int [][] directions = {
                    {1, 1},
                    {1, -1},
                    {-1, 1},
                    {-1, -1},
                    {1,0},
                    {0,1},
                    {-1,0},
                    {0,-1}
            };

            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                } else {
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPosition);
                    if (occupyingPiece == null || occupyingPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        if (piece.getPieceType() == PieceType.PAWN) {
            int [][] directions = {
                    {1, 0},
                    {1, 1},
                    {-1, 1},
                    {2, 0}
            };

            for (int[] dir : directions) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];
                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                } else {
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPosition);
                    if (col == 1 && occupyingPiece == null) {
                        break;
                    } else if (row == 2 && myPosition.getRow() != 2) {
                        break;
                    } else if (occupyingPiece == null || occupyingPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }


                }
            }
        }
        return moves;
    }
}
