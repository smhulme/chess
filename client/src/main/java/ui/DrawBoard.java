package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.io.PrintStream;

import static ui.EscapeSequences.*;

public class DrawBoard {
    private final ChessBoard board;

    public DrawBoard(ChessBoard board) {
        this.board = board;
    }

    public void draw(ChessGame.TeamColor perspective) {
        var out = System.out;
        out.println();
        drawHeaders(out, perspective);
        drawRows(out, perspective);
        drawHeaders(out, perspective);
        out.println();
        out.print(RESET_BG_COLOR);
        out.print(RESET_TEXT_COLOR);
    }

    private void drawHeaders(PrintStream out, ChessGame.TeamColor perspective) {
        out.print(SET_BG_COLOR_LIGHT_GREY);
        out.print(EMPTY); // Corner spacer

        String[] headers = {" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h "};
        if (perspective == ChessGame.TeamColor.BLACK) {
            for (int i = 7; i >= 0; i--) {
                printHeader(out, headers[i]);
            }
        } else {
            for (int i = 0; i < 8; i++) {
                printHeader(out, headers[i]);
            }
        }
        out.print(EMPTY);
        out.print(RESET_BG_COLOR);
        out.println();
    }

    private void printHeader(PrintStream out, String text) {
        out.print(SET_TEXT_COLOR_BLACK);
        out.print(text);
    }

    private void drawRows(PrintStream out, ChessGame.TeamColor perspective) {
        int startRow = (perspective == ChessGame.TeamColor.BLACK) ? 1 : 8;
        int endRow = (perspective == ChessGame.TeamColor.BLACK) ? 8 : 1;
        int rowStep = (perspective == ChessGame.TeamColor.BLACK) ? 1 : -1;

        for (int row = startRow; row != endRow + rowStep; row += rowStep) {
            // Row number prefix
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(" " + row + " ");
            
            for (int col = 1; col <= 8; col++) {
                int actualCol = (perspective == ChessGame.TeamColor.BLACK) ? (9 - col) : col;
                boolean isLightSquare = (row + actualCol) % 2 != 0;
                setSquareColor(out, isLightSquare);
                
                ChessPiece piece = board.getPiece(new ChessPosition(row, actualCol));
                printPiece(out, piece, isLightSquare);
            }
            
            // Row number suffix
            out.print(SET_BG_COLOR_LIGHT_GREY);
            out.print(SET_TEXT_COLOR_BLACK);
            out.print(" " + row + " ");
            out.print(RESET_BG_COLOR);
            out.println();
        }
    }

    private void setSquareColor(PrintStream out, boolean isLight) {
        if (isLight) {
            out.print(SET_BG_COLOR_WHITE); // Or Beige/Light color
        } else {
            out.print(SET_BG_COLOR_DARK_GREY); // Or Brown/Dark color
        }
    }

    private void printPiece(PrintStream out, ChessPiece piece, boolean isLightSquare) {
        if (piece == null) {
            out.print(EMPTY);
            return;
        }
        // Determine color of piece text (Red/Blue per your requirements or standard White/Black)
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            out.print(SET_TEXT_COLOR_RED);
        } else {
            out.print(SET_TEXT_COLOR_BLUE);
        }

        switch (piece.getPieceType()) {
            case KING -> out.print(isLightSquare ? BLACK_KING : WHITE_KING); 
            case QUEEN -> out.print(BLACK_QUEEN); 
            case BISHOP -> out.print(BLACK_BISHOP);
            case KNIGHT -> out.print(BLACK_KNIGHT);
            case ROOK -> out.print(BLACK_ROOK);
            case PAWN -> out.print(BLACK_PAWN);
        }
    }
}