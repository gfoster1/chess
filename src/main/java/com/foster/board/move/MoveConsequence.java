package com.foster.board.move;

import com.foster.board.Position;
import com.foster.board.piece.Piece;

import java.util.Optional;

public record MoveConsequence(Optional<Piece> capturedOpponentPiece, Piece movedPiece, Position previousPosition) {
}
