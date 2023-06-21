package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
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
        Piece piece = new Piece(Player.white, new Position(2, 3)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getWhitePieces()).thenReturn(Arrays.asList(
                new Pawn(Player.white, new Position(2, 1)),
                new Pawn(Player.white, new Position(2, 5))
        ));
        Stream<Move> builder = piece.generateMoves(false, 8, true, 8, board);

        List<Move> moves = builder.collect(Collectors.toList());
        Assertions.assertThat(moves).containsOnly(
                new Move(piece, new Position(0, 3)),
                new Move(piece, new Position(1, 3)),
                new Move(piece, new Position(3, 3)),
                new Move(piece, new Position(4, 3)),
                new Move(piece, new Position(5, 3)),
                new Move(piece, new Position(6, 3)),
                new Move(piece, new Position(7, 3)),
                new Move(piece, new Position(2, 2)),
                new Move(piece, new Position(2, 4))
        );
    }

    @Test
    public void testBlockedVerticalBySamePieces() {
        Piece piece = new Piece(Player.white, new Position(3, 2)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getWhitePieces()).thenReturn(Arrays.asList(
                new Pawn(Player.white, new Position(1, 2)),
                new Pawn(Player.white, new Position(5, 2))
        ));
        Stream<Move> builder = piece.generateMoves(false, 8, true, 8, board);

        List<Move> moves = builder.collect(Collectors.toList());
        Assertions.assertThat(moves).containsOnly(
                new Move(piece, new Position(2, 2)),
                new Move(piece, new Position(4, 2)),
                new Move(piece, new Position(3, 0)),
                new Move(piece, new Position(3, 1)),
                new Move(piece, new Position(3, 3)),
                new Move(piece, new Position(3, 4)),
                new Move(piece, new Position(3, 5)),
                new Move(piece, new Position(3, 6)),
                new Move(piece, new Position(3, 7))
        );
    }

    @Test
    public void testDiagonalPieces() {
        Piece piece = new Piece(Player.white, new Position(3, 2)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getWhitePieces())
                .thenReturn(Arrays.asList());
        Stream<Move> builder = piece.generateMoves(true, 8, false, 8, board);

        List<Move> actual = builder.collect(Collectors.toList());
        Move[] expected = {new Move(piece, new Position(4, 3)),
                new Move(piece, new Position(5, 4)),
                new Move(piece, new Position(6, 5)),
                new Move(piece, new Position(7, 6)),

                // + -
                new Move(piece, new Position(4, 1)),
                new Move(piece, new Position(5, 0)),

                // - -
                new Move(piece, new Position(2, 1)),
                new Move(piece, new Position(1, 0)),

                // - +
                new Move(piece, new Position(2, 3)),
                new Move(piece, new Position(1, 4)),
                new Move(piece, new Position(0, 5))};
        Assertions.assertThat(actual).containsOnly(expected);
    }

    @Test
    public void testBlockedDiagonalPieces() {
        Piece piece = new Piece(Player.white, new Position(2, 2)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        List<Piece> whitePieces = Arrays.asList(
                // + +
                new Pawn(Player.white, new Position(0, 0)),
                // + -
                new Pawn(Player.white, new Position(0, 4)),
                // - +
                new Pawn(Player.white, new Position(4, 0)),
                // - -
                new Pawn(Player.white, new Position(4, 4))
        );
        Mockito.when(board.getWhitePieces()).thenReturn(whitePieces);
        Stream<Move> builder = piece.generateMoves(true, 8, false, 8, board);

        List<Move> actual = builder.collect(Collectors.toList());
        Move[] expected = {
                // + +
                new Move(piece, new Position(1, 1)),

                // + -
                new Move(piece, new Position(1, 3)),

                // - +
                new Move(piece, new Position(3, 1)),

                // - -
                new Move(piece, new Position(3, 3))
        };
        Assertions.assertThat(actual).containsOnly(expected);
    }

    @Test
    public void blockedDiagonallyByOpposingPieces() {
        Piece piece = new Piece(Player.white, new Position(2, 2)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        List<Piece> blackPieces = Arrays.asList(
                // + +
                new Pawn(Player.black, new Position(0, 0)),
                // + -
                new Pawn(Player.black, new Position(0, 4)),
                // - +
                new Pawn(Player.black, new Position(4, 0)),
                // - -
                new Pawn(Player.black, new Position(4, 4))
        );
        Mockito.when(board.getBlackPieces()).thenReturn(blackPieces);
        Stream<Move> builder = piece.generateMoves(true, 8, false, 8, board);

        List<Move> actual = builder.collect(Collectors.toList());
        Move[] expected = {
                new Move(piece, new Position(0, 0)),
                new Move(piece, new Position(0, 4)),
                new Move(piece, new Position(4, 0)),
                new Move(piece, new Position(4, 4)),

                new Move(piece, new Position(1, 1)),
                new Move(piece, new Position(1, 3)),
                new Move(piece, new Position(3, 1)),
                new Move(piece, new Position(3, 3))
        };
        Assertions.assertThat(actual).containsOnly(expected);
    }

    @Test
    public void blockedHorizontallyByOpposingPieces() {
        Piece piece = new Piece(Player.white, new Position(2, 2)) {
            @Override
            public List<Move> possibleMoves(ChessBoard board) {
                return null;
            }
        };

        var board = Mockito.mock(ChessBoard.class);
        List<Piece> blackPieces = Arrays.asList(
                new Pawn(Player.black, new Position(0, 2)),
                new Pawn(Player.black, new Position(2, 0)),
                new Pawn(Player.black, new Position(2, 4)),
                new Pawn(Player.black, new Position(4, 2))
        );
        Mockito.when(board.getBlackPieces()).thenReturn(blackPieces);
        Stream<Move> builder = piece.generateMoves(false, 8, true, 8, board);

        List<Move> actual = builder.collect(Collectors.toList());
        Move[] expected = {
                new Move(piece, new Position(0, 2)),
                new Move(piece, new Position(2, 0)),
                new Move(piece, new Position(2, 4)),
                new Move(piece, new Position(4, 2)),

                new Move(piece, new Position(1, 2)),
                new Move(piece, new Position(2, 1)),
                new Move(piece, new Position(2, 3)),
                new Move(piece, new Position(3, 2))
        };
        Assertions.assertThat(actual).containsOnly(expected);
    }
}