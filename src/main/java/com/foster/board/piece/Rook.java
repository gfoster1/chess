package com.foster.board.piece;

import com.foster.ChessBoard;
import com.foster.Player;
import com.foster.board.Move;
import com.foster.board.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Rook extends Piece {

    public Rook(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard board) {
        return possibleStreamMoves(board).collect(Collectors.toList());
    }

    @Override
    public Stream<Move> possibleStreamMoves(ChessBoard board) {
        return generateMoves(false, 8, true, 8, board);
    }
}