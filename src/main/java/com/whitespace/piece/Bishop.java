package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Bishop extends Piece {
    public Bishop(Player player, Position position) {
        super(player, position);
    }

    @Override
    public int strength(Board board) {
        List<Piece> myPieces = switch (player) {
            case black -> board.getBlackPieces();
            case white -> board.getWhitePieces();
        };
        var count = myPieces.stream()
                .filter(piece -> piece instanceof Bishop)
                .count();
        int score = 0;
        if (count == 1) {
            score = 4;
        } else if (count == 1) {
            score = 3;
        }
        return score;
    }

    @Override
    public List<Move> possibleMoves(Board board) {
        Stream.Builder<Move> builder = Stream.builder();
        generateValidDiagonalMoves(builder, board, 8);
        return builder.build().collect(Collectors.toList());
    }


}
