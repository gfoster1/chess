package com.whitespace.board;


import com.whitespace.board.piece.Piece;
import com.whitespace.scoring.DefaultBoardService;

import java.util.Collections;
import java.util.List;

public class Board {
    public Board(DefaultBoardService blackBoardService, DefaultBoardService whiteBoardService) {
        
    }

    public List<Piece> getWhitePieces() {
        return Collections.emptyList();
    }

    public List<Piece> getBlackPieces() {
        return Collections.emptyList();
    }

    public void applyMove(Move myMove, boolean b) {

    }

    public void revertLastMove() {

    }

    public boolean isInvalidMove(Move move) {
        return false;
    }

    public void play() {
    }
}
