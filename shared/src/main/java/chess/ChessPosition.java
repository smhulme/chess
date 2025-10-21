package chess;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int column;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.column = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", row, column);
    }

    @Override
    public boolean equals(Object o) {
        // Check if the object references are the same
        if (this == o) {
            return true;
        }
        // Check if the other object is null or not the same class
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        // Cast the other object to ChessPosition
        ChessPosition that = (ChessPosition) o;
        // Compare row and column for equality
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

}