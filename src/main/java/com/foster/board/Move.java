package com.foster.board;


import com.foster.board.piece.Piece;

public record Move(Piece piece, Position destination) {
}
