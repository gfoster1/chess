package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PieceTest {
    @Test
    public void testPossibleHorizontalMoves() {
        Piece piece = new Piece(Player.white, new Position(1, 1)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Stream<Move> builder = piece.generateMoves(false, 8, true, 8, board);

        List<Move> moves = builder.collect(Collectors.toList());
        Assertions.assertThat(moves.size()).isEqualTo(14);
    }

    @Test
    public void testPossibleDiagonalMoves() {
        Piece piece = new Piece(Player.white, new Position(1, 1)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Stream<Move> builder = piece.generateMoves(true, 1, false, 1, board);

        List<Move> moves = builder.collect(Collectors.toList());
        Assertions.assertThat(moves.size()).isEqualTo(4);
    }

    @Test
    public void testInvalidMoves() {
        Piece piece = new Piece(Player.white, new Position(-1, 0)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Stream<Move> builder = piece.generateMoves(true, 8, true, 8, board);

        List<Move> moves = builder.toList();
        Assertions.assertThat(moves.size()).isEqualTo(15);
    }

    @Test
    public void testIfTakenBySame() {
        Piece piece = new Piece(Player.white, new Position(0, 0)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getWhitePieces()).thenReturn(Arrays.asList(
                new Pawn(Player.white, new Position(0, 0)),
                new Pawn(Player.white, new Position(0, 1)),
                new Pawn(Player.white, new Position(1, 1)),
                new Pawn(Player.white, new Position(1, 0))
        ));
        Stream<Move> builder = piece.generateMoves(true, 1, true, 1, board);

        Assertions.assertThat(builder.toList().size()).isEqualTo(0);
    }

    @Test
    public void testBlockedHorizontalBySamePieces() {
        Piece piece = new Piece(Player.white, new Position(2, 2)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getWhitePieces()).thenReturn(Arrays.asList(
                new Pawn(Player.white, new Position(2, 0)),
                new Pawn(Player.white, new Position(2, 4)),
                new Pawn(Player.white, new Position(0, 2)),
                new Pawn(Player.white, new Position(4, 2))
        ));
        Stream<Move> builder = piece.generateMoves(false, 8, true, 8, board);

        List<Move> moves = builder.collect(Collectors.toList());
        Assertions.assertThat(moves.size()).isEqualTo(4);
    }

    @Test
    public void testBlockedVertical() {

    }
}