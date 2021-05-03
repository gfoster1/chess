package com.whitespace.movement;

import com.whitespace.piece.Piece;

public record Move(Piece piece, Position destination) {
}
