package com.whitespace.scoring;

import com.whitespace.Player;
import com.whitespace.board.DefaultChessBoard;
import com.whitespace.board.Move;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class DefaultBoardServiceTest {

    @Test
    public void oneLevel() {
        DefaultBoardScoringService boardScoringService = new DefaultBoardScoringService();
        DefaultBoardService defaultBoardService = new DefaultBoardService(Player.white, boardScoringService, 2);
        DefaultChessBoard chessBoard = new DefaultChessBoard(defaultBoardService, defaultBoardService);
        Optional<Move> move = defaultBoardService.findBestMove(chessBoard);
    }
}