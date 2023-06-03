package com.whitespace;

import com.whitespace.board.Board;
import com.whitespace.scoring.DefaultBoardScoringService;
import com.whitespace.scoring.DefaultBoardService;

public class ChessApplication {
    public static void main(String[] args) {
        BoardScoringService boardScoringService = new DefaultBoardScoringService();
        var blackBoardService = new DefaultBoardService(Player.black, boardScoringService);
        var whiteBoardService = new DefaultBoardService(Player.white, boardScoringService);
        var board = new Board(blackBoardService, whiteBoardService);
        board.play();
    }
}
