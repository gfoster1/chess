package com.whitespace.piece;

import com.whitespace.movement.DefaultChessBoard;
import com.whitespace.ChessBoard;
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

    private boolean canSeeEachOther(Position p1, Position p2, DefaultChessBoard defaultChessBoard) {
        boolean count = defaultChessBoard.getPieces().parallelStream()
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
    public List<Move> possibleMoves(ChessBoard board) {
        // handle a castle
        return generateValidHorizontalMoves(Stream.builder(), board, 8)
                .build()
                .collect(Collectors.toList());
    }

}