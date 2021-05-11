package com.whitespace;

import com.whitespace.ai.CachingBoardScoringService;
import com.whitespace.ai.DefaultBestMoveService;
import com.whitespace.ai.DefaultBoardScoringService;
import com.whitespace.ai.FastScoringService;
import com.whitespace.movement.DefaultChessBoard;

public class ChessApplication {
    public static void main(String[] args) {
        var blackBoardService = new DefaultBestMoveService(Player.black, 1, new DefaultBoardScoringService(Player.black, 5));
        var whiteScoringService = new CachingBoardScoringService(new FastScoringService(Player.white, 3));
        var whiteBoardService = new DefaultBestMoveService(Player.white, 2, whiteScoringService);
        var board = new DefaultChessBoard(blackBoardService, whiteBoardService);
        board.play();
        whiteScoringService.writeToDisk();
    }
}
