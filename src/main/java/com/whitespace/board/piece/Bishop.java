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
        return possibleStreamMoves(board).collect(Collectors.toList());
    }

    @Override
    public Stream<Move> possibleStreamMoves(ChessBoard board) {
        return generateMoves(true, 8, false, 8, board);
    }
}
