package com.whitespace.ai;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.movement.Position;
import com.whitespace.piece.Knight;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

class CustomScorerTest {
    ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
    CustomScorer customScorer = new CustomScorer(Player.white, 3);

    @Test
    public void scoreKnights() {
        var position = new Position(3, 4);
        Mockito.when(chessBoard.getPosition(Mockito.anyInt(), Mockito.anyInt())).thenReturn(Optional.of(position));
        Mockito.when(chessBoard.isSpaceTakenByMyPiece(Mockito.any(), Mockito.any())).thenReturn(false);
        Knight knight = new Knight(Player.white, position);
        var score = customScorer.score(knight, Collections.emptyList());
        System.out.println("score = " + score);
        score = customScorer.score(knight, Collections.emptyList());
        System.out.println("score = " + score);
    }
}