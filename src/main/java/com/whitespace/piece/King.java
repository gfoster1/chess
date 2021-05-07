package com.whitespace.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King extends Piece {
    public King(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        Stream.Builder<Move> builder = Stream.builder();
        int size = 1;
        generateValidDiagonalMoves(builder, board, size);
        generateValidHorizontalMoves(builder, board, size);
        return builder.build().collect(Collectors.toList());
    }
}
