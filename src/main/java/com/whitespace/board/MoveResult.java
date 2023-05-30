package com.whitespace.board;

import com.whitespace.board.piece.Piece;

import java.util.Optional;

public record MoveResult(Piece piece,
                         Position origin,
                         Position destination,
                         Optional<Piece> capturedPiece,
                         boolean opponentWins,
                         boolean currentPlayerWins) {
}
