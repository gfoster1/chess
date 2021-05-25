package com.whitespace;

import com.whitespace.ai.movement.DecisionTreeBestMoveService;
import com.whitespace.ai.movement.DefaultBestMoveService;
import com.whitespace.ai.scoring.CachingBoardScoringService;
import com.whitespace.ai.scoring.DefaultBoardScoringService;
import com.whitespace.ai.scoring.FastBoardScoringService;
import com.whitespace.board.DefaultChessBoard;

public class ChessApplication {
    public static void main(String[] args) {
        var blackBoardService = new DefaultBestMoveService(Player.black, 1, new DefaultBoardScoringService(Player.black, 3));

        BestMoveService whiteBoardService;
        var fastScoringService = new FastBoardScoringService(Player.white, 0);
        boolean useCaching = false;
        int maxDepth = 3;
        if (useCaching) {
            var cachingBoardScoringService = new CachingBoardScoringService(fastScoringService);
            whiteBoardService = new DecisionTreeBestMoveService(Player.white, maxDepth, cachingBoardScoringService);
        } else {
            whiteBoardService = new DecisionTreeBestMoveService(Player.white, maxDepth, fastScoringService);
        }
//        var board = new DefaultChessBoard(blackBoardService, whiteBoardService);

        new DefaultChessBoard(blackBoardService, whiteBoardService).play();
//        new DefaultChessBoard(blackBoardService, whiteBoardService).play();
        if (useCaching) {
//        whiteScoringService.writeToDisk();
        }
    }
}
