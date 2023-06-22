package com.whitespace;

import com.whitespace.board.Move;
import com.whitespace.board.MoveResult;
import com.whitespace.board.Position;
import com.whitespace.board.piece.Piece;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface ChessBoard {
    void play();

    void revertLastMove();

    MoveResult applyMove(Move move, boolean debug);

    ChessBoard copy();

    boolean isInvalidMove(Move move);

    List<Piece> getBlackPieces();

    List<Piece> getWhitePieces();
}
