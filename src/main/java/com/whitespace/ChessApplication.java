package com.whitespace;

import com.whitespace.ai.CachingBoardScoringService;
import com.whitespace.ai.DefaultBestMoveService;
import com.whitespace.ai.DefaultBoardScoringService;

public class ChessApplication {
    public static void main(String[] args) {
        var blackBoardService = new DefaultBestMoveService(Player.black, 1, new DefaultBoardScoringService(Player.black, 5));
        var whiteScoringService = new CachingBoardScoringService(Player.white, 5);
        var whiteBoardService = new DefaultBestMoveService(Player.white, 2, whiteScoringService);
        var board = new Board(blackBoardService, whiteBoardService);
        board.play();
        whiteScoringService.writeToDisk();
    }
}
