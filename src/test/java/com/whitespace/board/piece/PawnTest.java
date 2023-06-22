package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;

class PawnTest {
    @Test
    public void possibleStarting2PositionMoves() {
        ChessBoard chessBoard = Mockito.mock(ChessBoard.class);

        Pawn pawn = new Pawn(Player.white, new Position(1, 1));
        var actual = pawn.possibleMoves(chessBoard);
        Move[] expected = {
                new Move(pawn, new Position(2, 1)),
                new Move(pawn, new Position(3, 1))
        };
        Assertions.assertThat(actual).containsOnly(expected);
    }

    @Test
    public void possibleStarting1PositionMoves() {
        ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
        Mockito.when(chessBoard.getBlackPieces()).thenReturn(Arrays.asList(
                new Pawn(Player.black, new Position(3, 1))
        ));
        Mockito.when(chessBoard.getWhitePieces()).thenReturn(Arrays.asList(
                new Pawn(Player.white, new Position(3, 1))
        ));

        Pawn pawn = new Pawn(Player.white, new Position(1, 1));
        var actual = pawn.possibleMoves(chessBoard);
        Move[] expected = {
                new Move(pawn, new Position(2, 1))
        };
        Assertions.assertThat(actual).containsOnly(expected);
    }

    @Test
    public void possibleStarting0PositionMoves() {
        ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
        Mockito.when(chessBoard.getBlackPieces()).thenReturn(Arrays.asList(
                new Pawn(Player.black, new Position(2, 1))
        ));
        Mockito.when(chessBoard.getWhitePieces()).thenReturn(Arrays.asList(
                new Pawn(Player.white, new Position(2, 1))
        ));

        Pawn pawn = new Pawn(Player.white, new Position(1, 1));
        var actual = pawn.possibleMoves(chessBoard);
        Move[] expected = {};
        Assertions.assertThat(actual).containsOnly(expected);
    }

    @Test
    public void takeOpponentPieces() {
        ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
        Mockito.when(chessBoard.getBlackPieces()).thenReturn(Arrays.asList(
                new Pawn(Player.black, new Position(2, 0)),
                new Pawn(Player.black, new Position(2, 2))
        ));

        Pawn pawn = new Pawn(Player.white, new Position(1, 1));
        var actual = pawn.possibleMoves(chessBoard);
        Move[] expected = {
                new Move(pawn, new Position(2, 0)),
                new Move(pawn, new Position(2, 2))
        };
        Assertions.assertThat(actual).contains(expected);
    }
}