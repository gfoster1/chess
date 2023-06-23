package com.whitespace;

import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.List;
import java.util.Optional;

public interface ChessBoard {
    void play();

    Optional<String> applyMove(Move move, boolean debug);

    List<Piece> getBlackPieces();

    List<Piece> getWhitePieces();

    Optional<String> rollbackToPreviousMove();
}
