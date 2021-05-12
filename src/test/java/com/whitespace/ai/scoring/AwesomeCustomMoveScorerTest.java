package com.whitespace.ai.scoring;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Position;
import com.whitespace.board.piece.Knight;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

class AwesomeCustomMoveScorerTest {
    ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
    AwesomeCustomMoveScorer awesomeCustomMoveScorer = new AwesomeCustomMoveScorer(Player.white, 3);

    @Test
    public void scoreKnights() {
        var position = new Position(3, 4);
        Mockito.when(chessBoard.getPosition(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.of(position));
        Mockito.when(chessBoard.isSpaceTakenByMyPiece(Mockito.any(), Mockito.any())).thenReturn(false);
        Knight knight = new Knight(Player.white, position);
        var score = awesomeCustomMoveScorer.score(knight, Collections.emptyList());
        System.out.println("score = " + score);
        score = awesomeCustomMoveScorer.score(knight, Collections.emptyList());
        System.out.println("score = " + score);
    }
}