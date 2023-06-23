package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Position;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

class KnightTest {
    @Test
    public void filterRowsColumnsToRemainWithinBoard() {
        Knight knight = new Knight(Player.white, new Position(0, 0));
        ChessBoard board = Mockito.mock(ChessBoard.class);
        var actual = knight.possibleStreamMoves(board)
                .map(move -> move.destination())
                .filter(position -> position.column() < 0 || position.column() > 8 || position.row() < 0 || position.row() > 8)
                .count();
        long expected = 0;
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void allMovesAreValid() {
        Knight knight = new Knight(Player.white, new Position(3, 3));
        ChessBoard board = Mockito.mock(ChessBoard.class);
        var actual = knight.possibleStreamMoves(board).count();
        long expected = 8;
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void allValidMovesAreTakenBySamePieces() {
        ChessBoard board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getWhitePieces()).thenReturn(
                List.of(new Pawn(Player.white, new Position(5, 4)),
                        new Pawn(Player.white, new Position(5, 2)),
                        new Pawn(Player.white, new Position(1, 4)),
                        new Pawn(Player.white, new Position(1, 2)),
                        new Pawn(Player.white, new Position(4, 5)),
                        new Pawn(Player.white, new Position(4, 1)),
                        new Pawn(Player.white, new Position(2, 5)),
                        new Pawn(Player.white, new Position(2, 1))
                )
        );
        Knight knight = new Knight(Player.white, new Position(3, 3));
        var actual = knight.possibleStreamMoves(board).count();
        long expected = 0;
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}