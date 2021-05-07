package com.whitespace.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        List<Move> possibleMoves = new ArrayList<>(4);
        final var piece = this;
        var row = position.row() + 2;
        var column = position.column() + 1;
        board.getPosition(row, column).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });

        row = position.row() - 2;
        column = position.column() + 1;
        board.getPosition(row, column).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });

        row = position.row() + 1;
        column = position.column() + 2;
        board.getPosition(row, column).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });

        row = position.row() + 1;
        column = position.column() - 2;
        board.getPosition(row, column).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        return possibleMoves;
    }
}
