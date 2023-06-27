package com.foster;

import com.foster.board.Move;
import com.foster.board.piece.Piece;

import java.util.List;
import java.util.Optional;

public interface ChessBoard {
    void play();

    Optional<String> applyMove(Move move, boolean debug);

    List<Piece> getBlackPieces();

    List<Piece> getWhitePieces();

    Optional<String> rollbackToPreviousMove(boolean includeFEN);

    public String translateToFEN();
}
