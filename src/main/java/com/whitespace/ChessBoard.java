package com.whitespace;

import com.whitespace.movement.Move;
import com.whitespace.movement.MoveResult;
import com.whitespace.movement.Position;
import com.whitespace.piece.Piece;

import java.util.Collection;
import java.util.Optional;

public interface ChessBoard {
    void play();

    void revertLastMove();

    MoveResult applyMove(Move move, boolean debug);

    Optional<Position> getPosition(int row, int column);

    boolean isSpaceTakenByMyPiece(Position destination, Player player);

    boolean isSpaceTakenByOpposingPlayerPiece(Position destination, Player player);

    boolean isSpaceTaken(Position position);

    Collection<Piece> getPieces();
}
