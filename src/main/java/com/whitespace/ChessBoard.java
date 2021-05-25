package com.whitespace;

import com.whitespace.board.Move;
import com.whitespace.board.MoveResult;
import com.whitespace.board.Position;
import com.whitespace.board.piece.Piece;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface ChessBoard {
    void play();

    void revertLastMove();

    MoveResult applyMove(Move move, boolean debug);

    Optional<Position> getPosition(int row, int column);

    boolean isSpaceTakenByMyPiece(Position destination, Player player);

    boolean isSpaceTakenByOpposingPlayerPiece(Position destination, Player player);

    boolean isSpaceTaken(Position position);

    Collection<Piece> getPieces();

    Stream<Move> getPossibleMoves(Player player);
}
