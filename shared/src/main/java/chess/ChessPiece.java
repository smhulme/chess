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
                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
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
                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPosition = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPosition);
                    if (occupyingPiece == null || occupyingPiece.getTeamColor() != this.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                }
            }
        }

        if (piece.getPieceType() == PieceType.PAWN) {
            int direction = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1; // if color is white, move up, if black, move down
            int startRow = (this.getTeamColor() == ChessGame.TeamColor.WHITE) ? 2 : 7; // if color white, start on row 2, if not, row 7

            ChessPiece.PieceType[] promotionPieceTypes = {
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT
            };

            //forward direction movement
            int row = myPosition.getRow() + direction;
            int col = myPosition.getColumn();

            if (row >= 1 && row <= 8) {
                ChessPosition newPosition = new ChessPosition(row, col);
                if (board.getPiece(newPosition) == null) {
                    if ((this.getTeamColor() == ChessGame.TeamColor.WHITE && row == 8) || (this.getTeamColor() == ChessGame.TeamColor.BLACK && row == 1)) {
                        for (ChessPiece.PieceType promo :promotionPieceTypes) {
                            moves.add(new ChessMove(myPosition, newPosition, promo));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    
                }
                //double forward direction from start line
                if (myPosition.getRow() == startRow) {
                    int doubleMove = myPosition.getRow() + 2 * direction;
                    ChessPosition doubleNewPosition = new ChessPosition(doubleMove, col);
                    ChessPosition singleForward = new ChessPosition(myPosition.getRow() + direction, col);
                    if (board.getPiece(singleForward) == null && board.getPiece(doubleNewPosition) == null) {
                        moves.add(new ChessMove(myPosition, doubleNewPosition, null));
                    }
                }
            }

            for (int i = -1; i <= 1; i += 2) {
                int enemyCol = myPosition.getColumn() + i;
                if (enemyCol >= 1 && enemyCol <= 8 && row >= 1 && row <= 8) {
                    ChessPosition capturePos = new ChessPosition(row, enemyCol);
                    ChessPiece occuypingPiece = board.getPiece(capturePos);
                    if (occuypingPiece != null && occuypingPiece.getTeamColor() != this.getTeamColor()){
                        if ((this.getTeamColor() == ChessGame.TeamColor.WHITE && row == 8) || (this.getTeamColor() == ChessGame.TeamColor.BLACK && row == 1)) {
                            for (ChessPiece.PieceType promo :promotionPieceTypes) {
                                moves.add(new ChessMove(myPosition, capturePos, promo));
                            }
                    } else {
                        moves.add(new ChessMove(myPosition, capturePos, null));
                    }
                    }
                }
            }
        }
        return moves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessPiece that = (ChessPiece) o;
        return this.type == that.type && this.pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (pieceColor != null ? pieceColor.hashCode() : 0);
        return result;
    }
}
