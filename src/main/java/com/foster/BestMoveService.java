package com.foster;

import com.foster.board.Move;

import java.util.Optional;

public interface BestMoveService {
    Optional<Move> findBestMove(ChessBoard chessBoard);
}
