package com.whitespace;

import com.whitespace.piece.DefaultBoardService;

public class ChessApplication {
    public static void main(String[] args) {
        var blackBoardService = new DefaultBoardService(Player.black);
        var whiteBoardService = new DefaultBoardService(Player.white);
        var board = new Board(blackBoardService, whiteBoardService);
        board.play();
    }
}
