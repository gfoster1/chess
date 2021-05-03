package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Queen extends Piece {

    public Queen(Player player, Position position) {
        super(player, position);
    }

    @Override
    public int strength(Board board) {
        return 10;
    }

    @Override
    public List<Move> possibleMoves(Board board) {
        Stream.Builder<Move> builder = Stream.builder();
        generateValidDiagonalMoves(builder, board);
        generateValidHorizontalMoves(builder, board);
        return builder.build().collect(Collectors.toList());
    }
}