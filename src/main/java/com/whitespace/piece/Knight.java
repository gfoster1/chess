package com.whitespace.piece;

import com.whitespace.Board;
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
    public int strength(Board board) {
        var myPieces = switch (player) {
            case black -> board.getBlackPieces();
            case white -> board.getWhitePieces();
        };
        var count = myPieces.stream()
                .filter(piece -> piece instanceof Knight)
                .count();
        int score = 0;

        if (count == 1) {
            score = 4;
        } else if (count == 1) {
            score = 3;
        }
        return score;
    }

    @Override
    public List<Move> possibleMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
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
