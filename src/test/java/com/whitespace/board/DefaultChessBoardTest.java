package com.whitespace.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultChessBoardTest {

    @Test
    void testHashCode() {
        assertEquals(new DefaultChessBoard(null, null).hashCode(),
                new DefaultChessBoard(null, null).hashCode());
    }
}