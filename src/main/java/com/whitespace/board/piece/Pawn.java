package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Pawn extends Piece {
    private final Position startingPosition;

    public Pawn(Player player, Position position) {
        super(player, position);
        startingPosition = position;
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        return possibleStreamMoves(board).collect(Collectors.toList());
    }

    @Override
    public Stream<Move> possibleStreamMoves(ChessBoard chessBoard) {
        Stream.Builder<Move> builder = Stream.builder();
        int multipler = switch (player) {
            case white -> -1;
            case black -> 1;
        };

        if (position.equals(startingPosition)) {
            var p1 = new Position(startingPosition.row() + (1 * multipler), startingPosition.column());
            var p2 = new Position(startingPosition.row() + (2 * multipler), startingPosition.column());
            if (areSpacesOpen(chessBoard, p1, p2)) {
                builder.add(new Move(this, p2));
            }
        }

        int row = position.row() + (1 * multipler);
        int maxBoardSize = 8;
        if (row >= 0 && row < maxBoardSize) {
            var p1 = new Position(row, position.column());
            if (areSpacesOpen(chessBoard, p1)) {
                builder.add(new Move(this, p1));
            }

            if (position.column() + 1 < maxBoardSize) {
                var p2 = new Position(row, position.column() + 1);
                if (isSpaceTakenByOpposingPlayerPiece(chessBoard, p2)) {
                    builder.add(new Move(this, p2));
                }
            }

            if (position.column() - 1 >= 0) {
                var p3 = new Position(row, position.column() - 1);
                if (isSpaceTakenByOpposingPlayerPiece(chessBoard, p3)) {
                    builder.add(new Move(this, p3));
                }
            }
        }
        return builder.build();
    }

    private boolean areSpacesOpen(ChessBoard chessBoard, Position... positions) {
        return Stream.concat(chessBoard.getWhitePieces().parallelStream(), chessBoard.getBlackPieces().parallelStream())
                .map(piece -> piece.getPosition())
                .filter(p -> {
                    for (Position position : positions) {
                        if (position.row() == p.row() && position.column() == p.column()) {
                            return true;
                        }
                    }
                    return false;
                })
                .findAny()
                .isEmpty();
    }

    private boolean isSpaceTakenByOpposingPlayerPiece(ChessBoard chessBoard, Position position) {
        var myPieces = switch (player) {
            case black -> chessBoard.getWhitePieces();
            case white -> chessBoard.getBlackPieces();
        };
        return myPieces.parallelStream()
                .map(piece -> piece.getPosition())
                .filter(p -> p.column() == position.column() && p.row() == position.row())
                .findAny()
                .isPresent();
    }
}
