package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Rook extends Piece {

    public Rook(Player player, Position position) {
        super(player, position);
    }

    @Override
    public int strength(Board board) {
        List<Piece> myPieces = switch (player) {
            case black -> board.getBlackPieces();
            case white -> board.getWhitePieces();
        };
        List<Piece> rooks = myPieces.stream()
                .filter(piece -> piece instanceof Rook)
                .collect(Collectors.toList());
        int twoRookMultipler = switch (rooks.size()) {
            case 2 -> 6 + (canSeeEachOther(rooks.get(0).position, rooks.get(1).position, board) ? 2 : 0);
            case 1 -> 5;
            default -> 0;
        };

        return twoRookMultipler;
    }

    private boolean canSeeEachOther(Position p1, Position p2, Board board) {
        Stream<Piece> pieces = Stream.concat(board.getBlackPieces().stream(), board.getWhitePieces().stream());
        boolean count = pieces
                .filter(piece -> piece.getPosition() != p1 && piece.getPosition() != p2)
                .filter(piece -> {
                    var p3 = piece.getPosition();
                    if (p1.column() == p3.column() && p2.column() == p3.column()) {
                        // h
                        // same row
                        List<Position> positions = Arrays.asList(p1, p2, p3);
                        positions.sort(Comparator.comparingInt(Position::row));
                        if (positions.get(1).equals(p3)) {
                            return false;
                        }
                    } else if (p1.row() == p3.row() && p2.row() == p3.row()) {
                        // column
                        // same row
                        List<Position> positions = Arrays.asList(p1, p2, p3);
                        positions.sort(Comparator.comparingInt(Position::column));
                        if (positions.get(1).equals(p3)) {
                            return false;
                        }
                    }
                    return true;
                })
                .count() == 0;
        return count;
    }

    @Override
    public List<Move> possibleMoves(Board board) {
        // handle a castle
        return generateValidHorizontalMoves(Stream.builder(), board, 8)
                .build()
                .collect(Collectors.toList());
    }

}