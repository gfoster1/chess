package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pawn extends Piece {
    private static final Map<Integer, Integer> blackValues = new HashMap<>();
    private static final Map<Integer, Integer> whiteValues = new HashMap<>();

    public Pawn(Player player, Position position) {
        super(player, position);
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
    public List<Move> possibleMoves(Board board) {
        var piece = this;
        List<Move> moves = new ArrayList<>();
        int multipler = switch (player) {
            case white -> 1;
            case black -> -1;
        };
        int row = position.row() + (1 * multipler);
        int maxBoardSize = 7;
        if (row < 0 || row > maxBoardSize) {
            return moves;
        }

        board.getPosition(row, position.column())
                .ifPresent(destination -> {
                    if (!board.isSpaceTakenByMyPiece(destination, player)
                            && !board.isSpaceTakenByOpposingPlayerPiece(destination, player)) {
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

        int whiteStartingPosition = 1;
        int blackStartingPosition = 6;
        if ((player.equals(Player.white) && position.row() == whiteStartingPosition) || (player.equals(Player.black) && position.row() == blackStartingPosition)) {
            row = position.row() + (2 * multipler);
            if (row > 6 || row < 0) {
                System.out.println("problem pawn");
            }
            board.getPosition(row, position.column())
                    .ifPresent(destination -> {
                        if (!board.isSpaceTakenByMyPiece(destination, player)
                                && !board.isSpaceTakenByOpposingPlayerPiece(destination, player)) {
                            moves.add(new Move(piece, destination));
                        }
                    });
        }
        return moves;
    }
}
