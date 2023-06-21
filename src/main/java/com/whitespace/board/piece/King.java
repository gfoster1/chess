package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class King extends Piece {
    public King(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard chessBoard) {
        return possibleStreamMoves(chessBoard).collect(Collectors.toList());
    }

    @Override
    public Stream<Move> possibleStreamMoves(ChessBoard board) {
        return generateMoves(true, 1, true, 1, board);
    }
}
