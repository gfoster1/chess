package com.whitespace.board;

import com.whitespace.board.piece.Piece;

public record Move(Piece piece, Position destination) {
}
