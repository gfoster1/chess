package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Knight extends Piece {
    public Knight(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        return possibleStreamMoves(board).collect(Collectors.toList());
    }

    @Override
    public Stream<Move> possibleStreamMoves(ChessBoard board) {
        var row = position.row();
        var column = position.column();

        Stream.Builder<Move> builder = Stream.builder();
        builder.add(new Move(this, new Position(row + 2, column + 1)));
        builder.add(new Move(this, new Position(row + 2, column - 1)));
        builder.add(new Move(this, new Position(row - 2, column + 1)));
        builder.add(new Move(this, new Position(row - 2, column - 1)));
        builder.add(new Move(this, new Position(row + 1, column + 2)));
        builder.add(new Move(this, new Position(row + 1, column - 2)));
        builder.add(new Move(this, new Position(row - 1, column + 2)));
        builder.add(new Move(this, new Position(row - 1, column - 2)));

        var myPositions = switch (player) {
            case black -> board.getBlackPieces();
            case white -> board.getWhitePieces();
        };

        return builder.build()
                .filter(move -> myPositions.parallelStream()
                        .map(piece -> piece.getPosition())
                        .filter(position -> {
                            var dest = move.destination();
                            return position.column() == dest.column() && position.row() == dest.row();
                        })
                        .findAny()
                        .isEmpty())
                .filter(move -> {
                    var destination = move.destination();
                    var lowerLimit = 0;
                    var upperLimit = 8;
                    return (destination.row() >= lowerLimit && destination.row() < upperLimit)
                            && (destination.column() >= lowerLimit && destination.column() < upperLimit);
                })
                .parallel();
    }
}
