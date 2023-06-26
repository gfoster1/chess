package com.whitespace.board.move;

import com.whitespace.board.Position;
import com.whitespace.board.piece.Piece;

import java.util.Optional;

public record MoveConsequence(Optional<Piece> capturedOpponentPiece, Piece movedPiece, Position previousPosition) {
}
