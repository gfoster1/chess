package com.whitespace.board.piece;

import com.whitespace.Player;
import com.whitespace.board.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PawnTest {
    @Test
    public void possibleStartingPositionMoves() {
        Pawn pawn = new Pawn(Player.white, new Position(1,1));

    }
}