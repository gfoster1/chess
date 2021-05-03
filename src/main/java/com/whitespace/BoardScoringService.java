package com.whitespace;

import com.whitespace.movement.Move;

import java.util.Optional;

public interface BoardScoringService {
    Optional<Move> findBestMove(Board board);
}
