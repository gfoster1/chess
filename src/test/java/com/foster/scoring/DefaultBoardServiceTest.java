package com.foster.scoring;

import com.foster.Player;
import com.foster.board.DefaultChessBoard;
import com.foster.board.Move;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class DefaultBoardServiceTest {

    @Test
    public void oneLevel() {
        DefaultBoardScoringService boardScoringService = new DefaultBoardScoringService();
        DefaultBoardService whiteBoardService = new DefaultBoardService(Player.white, boardScoringService, 2);
        DefaultBoardService blackBoardService = new DefaultBoardService(Player.black, boardScoringService, 1);
        DefaultChessBoard chessBoard = new DefaultChessBoard(blackBoardService, whiteBoardService, "8/p7/8/8/8/8/P/8 b KQkq e3 0 1");
        Optional<Move> move = whiteBoardService.findBestMove(chessBoard);
    }

    @Test
    public void deepInADefaultBoard() {
        DefaultBoardScoringService boardScoringService = new DefaultBoardScoringService();
        DefaultBoardService whiteBoardService = new DefaultBoardService(Player.white, boardScoringService, 2);
        DefaultBoardService blackBoardService = new DefaultBoardService(Player.black, boardScoringService, 8);
        DefaultChessBoard chessBoard = new DefaultChessBoard(blackBoardService, whiteBoardService);
        Optional<Move> move = whiteBoardService.findBestMove(chessBoard);
    }
}