package com.whitespace;

import com.whitespace.ai.movement.DefaultBestMoveService;
import com.whitespace.ai.scoring.CachingBoardScoringService;
import com.whitespace.ai.scoring.DefaultBoardScoringService;
import com.whitespace.ai.scoring.FastScoringService;
import com.whitespace.board.DefaultChessBoard;

public class ChessApplication {
    public static void main(String[] args) {
        var blackBoardService = new DefaultBestMoveService(Player.black, 1, new DefaultBoardScoringService(Player.black, 5));

        BestMoveService whiteBoardService;
        boolean useCaching = false;
        if (useCaching) {
            var whiteScoringService = new CachingBoardScoringService(new FastScoringService(Player.white, 3));
            whiteBoardService = new DefaultBestMoveService(Player.white, 2, whiteScoringService);
        } else {
            whiteBoardService = new DefaultBestMoveService(Player.white, 2, new FastScoringService(Player.white, 3));
        }
        var board = new DefaultChessBoard(blackBoardService, whiteBoardService);

        board.play();

        if (useCaching) {
//        whiteScoringService.writeToDisk();
        }
    }
}
