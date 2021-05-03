package com.whitespace;

import com.whitespace.movement.Move;

import java.util.Optional;

public interface BoardService {
    Optional<Move> findBestMove(Board board);
}
