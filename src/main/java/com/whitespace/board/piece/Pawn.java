package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pawn extends Piece {
    private static final Map<Integer, Integer> blackValues = new HashMap<>();
    private static final Map<Integer, Integer> whiteValues = new HashMap<>();
    private final Position startingPosition;

    public Pawn(Player player, Position position) {
        super(player, position);
        startingPosition = position;

        int queensStrength = 10;
        whiteValues.put(0, 1);
        whiteValues.put(1, 1);
        whiteValues.put(2, 2);
        whiteValues.put(3, 3);
        whiteValues.put(4, 4);
        whiteValues.put(5, 5);
        whiteValues.put(6, 6);
        whiteValues.put(7, queensStrength);

        blackValues.put(7, 1);
        blackValues.put(6, 1);
        blackValues.put(5, 2);
        blackValues.put(4, 3);
        blackValues.put(3, 4);
        blackValues.put(2, 5);
        blackValues.put(1, 6);
        blackValues.put(0, queensStrength);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        var piece = this;
        List<Move> moves = new ArrayList<>(4);
        int multipler = switch (player) {
            case white -> 1;
            case black -> -1;
        };


        if (position.equals(startingPosition)) {
            var p1 = board.getPosition(startingPosition.row() + (1 * multipler), startingPosition.column())
                    .get();
            var p2 = board.getPosition(startingPosition.row() + (2 * multipler), startingPosition.column())
                    .get();
            if (!board.isSpaceTaken(p1) && !board.isSpaceTaken(p2)) {
                moves.add(new Move(piece, p2));
            }
        }

        int row = position.row() + (1 * multipler);
        board.getPosition(row, position.column())
                .ifPresent(destination -> {
                    if (!board.isSpaceTaken(destination)) {
                        moves.add(new Move(piece, destination));
                    }
                });

        board.getPosition(row, position.column() + 1)
                .ifPresent(destination -> {
                    if (board.isSpaceTakenByOpposingPlayerPiece(destination, player)) {
                        moves.add(new Move(piece, destination));
                    }
                });

        board.getPosition(row, position.column() - 1)
                .ifPresent(destination -> {
                    if (board.isSpaceTakenByOpposingPlayerPiece(destination, player)) {
                        moves.add(new Move(piece, destination));
                    }
                });
        return moves;
    }
}
