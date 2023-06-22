package com.whitespace;

import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.List;

public interface ChessBoard {
    void play();

    ChessBoard applyMove(Move move, boolean debug);

    List<Piece> getBlackPieces();

    List<Piece> getWhitePieces();
}
