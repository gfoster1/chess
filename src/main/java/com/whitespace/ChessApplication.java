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

        var maxDepth = 3;
        var useCaching = false;
        var fastScoringService = new FastBoardScoringService(Player.white, 0);
        var cachingBoardScoringService = new CachingBoardScoringService(fastScoringService);
        BestMoveService whiteBoardService;
        if (useCaching) {
            whiteBoardService = new DecisionTreeBestMoveService(Player.white, maxDepth, cachingBoardScoringService);
        } else {
            whiteBoardService = new DecisionTreeBestMoveService(Player.white, maxDepth, fastScoringService);
        }
//        var board = new DefaultChessBoard(blackBoardService, whiteBoardService);

        new DefaultChessBoard(blackBoardService, whiteBoardService).play();

        if (useCaching) {
            cachingBoardScoringService.printStats();
        }
//        new DefaultChessBoard(blackBoardService, whiteBoardService).play();
        if (useCaching) {
//        whiteScoringService.writeToDisk();
        }
    }
}
