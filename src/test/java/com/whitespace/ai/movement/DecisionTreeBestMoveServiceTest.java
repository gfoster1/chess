package com.whitespace.ai.movement;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class DecisionTreeBestMoveServiceTest {
    DecisionTreeBestMoveService decisionTreeBestMoveService;

    @Test
    public void findStuff() {
        ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
        BoardScoreService boardScoreService = Mockito.mock(BoardScoreService.class);
        decisionTreeBestMoveService = new DecisionTreeBestMoveService(Player.white, 2, boardScoreService);
        Optional<Move> bestMove = decisionTreeBestMoveService.findBestMove(chessBoard);
    }
}