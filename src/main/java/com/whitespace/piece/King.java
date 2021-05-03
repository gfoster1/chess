package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(Player player, Position position) {
        super(player, position);
    }

    @Override
    public int strength(Board board) {
        return 200;
    }

    @Override
    public List<Move> possibleMoves(Board board) {
        var piece = this;
        List<Move> possibleMoves = new ArrayList<>();
        generateDiagnalMoves(board, piece, possibleMoves, position.row(), position.column());
        generateHorizontalMoves(board, piece, possibleMoves, position.row(), position.column());
        return possibleMoves;
    }

    private void generateHorizontalMoves(Board board, King piece, List<Move> possibleMoves, int row, int column) {
        board.getPosition(row + 1, column).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        board.getPosition(row, column + 1).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        board.getPosition(row - 1, column).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        board.getPosition(row, column - 1).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
    }

    private void generateDiagnalMoves(Board board, King piece, List<Move> possibleMoves, int x, int y) {
        board.getPosition(x + 1, y + 1).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        board.getPosition(x - 1, y + 1).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        board.getPosition(x + 1, y - 1).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
        board.getPosition(x - 1, y - 1).ifPresent(destination -> {
            if (!board.isSpaceTakenByMyPiece(destination, player)) {
                possibleMoves.add(new Move(piece, destination));
            }
        });
    }
}
