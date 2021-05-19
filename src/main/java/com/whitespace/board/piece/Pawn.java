package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    private final Position startingPosition;

    public Pawn(Player player, Position position) {
        super(player, position);
        startingPosition = position;
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
            var p1 = new Position(startingPosition.row() + (1 * multipler), startingPosition.column());
            var p2 = new Position(startingPosition.row() + (2 * multipler), startingPosition.column());
            if (!board.isSpaceTaken(p1) && !board.isSpaceTaken(p2)) {
                moves.add(new Move(piece, p2));
            }
        }

        int row = position.row() + (1 * multipler);
        int maxBoardSize = 8;
        if (row >= 0 && row < maxBoardSize) {
            var p1 = new Position(row, position.column());
            if (!board.isSpaceTaken(p1)) {
                moves.add(new Move(piece, p1));
            }

            if (position.column() + 1 < maxBoardSize) {
                var p2 = new Position(row, position.column() + 1);
                if (board.isSpaceTakenByOpposingPlayerPiece(p2, player)) {
                    moves.add(new Move(piece, p2));
                }
            }

            if (position.column() - 1 >= 0) {
                var p3 = new Position(row, position.column() - 1);
                if (board.isSpaceTakenByOpposingPlayerPiece(p3, player)) {
                    moves.add(new Move(piece, p3));
                }
            }
        }
        return moves;
    }
}
