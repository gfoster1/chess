package com.whitespace;

import com.whitespace.board.Move;

import java.util.Optional;

public interface BestMoveService {
    Optional<Move> findBestMove(ChessBoard chessBoard);
}
