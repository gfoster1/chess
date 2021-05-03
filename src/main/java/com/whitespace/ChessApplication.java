package com.whitespace;

import com.whitespace.piece.DefaultBoardScoringService;

public class ChessApplication {
    public static void main(String[] args) {
        var blackBoardService = new DefaultBoardScoringService(Player.black);
        var whiteBoardService = new DefaultBoardScoringService(Player.white);
        var board = new Board(blackBoardService, whiteBoardService);
        board.play();
    }
}
