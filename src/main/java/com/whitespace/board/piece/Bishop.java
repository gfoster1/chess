package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bishop extends Piece {
    public Bishop(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        return Collections.emptyList();
//        Stream.Builder<Move> builder = Stream.builder();
//        generateValidDiagonalMoves(builder, board, 8);
//        return builder.build().collect(Collectors.toList());
    }
}
