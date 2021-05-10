package com.whitespace;

import com.whitespace.movement.Move;

import java.util.Optional;

public interface BestMoveService {
    Optional<Move> findBestMove(ChessBoard chessBoard);
}
