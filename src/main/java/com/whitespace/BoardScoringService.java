package com.whitespace;

import com.whitespace.piece.Piece;

import java.util.List;

public interface BoardScoringService {
    /**
     * @return 0 > Integer.MAX score of the board strength. If the score is Integer.MIN | Max than that side
     * won or lost.
     */
    int scoreBoard(Board board, Player plLayer);

    int scorePieces(List<Piece> myPieces,    List<Piece> opponentsPieces);
}
