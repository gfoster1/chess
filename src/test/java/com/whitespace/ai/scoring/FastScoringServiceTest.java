package com.whitespace.ai.scoring;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Position;
import com.whitespace.board.piece.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;

class FastScoringServiceTest {
    FastScoringService fastScoringService = new FastScoringService(Player.white, 3);

    @Test
    public void testBoard() {
        Collection<Piece> pieces = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            pieces.add(new Pawn(Player.white, new Position(1, i)));
            pieces.add(new Pawn(Player.black, new Position(6, i)));
        }

        pieces.add(new Rook(Player.white, new Position(0, 0)));
        pieces.add(new Knight(Player.white, new Position(0, 1)));
        pieces.add(new Bishop(Player.white, new Position(0, 2)));
        pieces.add(new Queen(Player.white, new Position(0, 3)));
        pieces.add(new King(Player.white, new Position(0, 4)));
        pieces.add(new Bishop(Player.white, new Position(0, 5)));
        pieces.add(new Knight(Player.white, new Position(0, 6)));
        pieces.add(new Rook(Player.white, new Position(0, 7)));

        pieces.add(new Rook(Player.black, new Position(7, 0)));
        pieces.add(new Knight(Player.black, new Position(7, 1)));
        pieces.add(new Bishop(Player.black, new Position(7, 2)));
        pieces.add(new Queen(Player.black, new Position(7, 3)));
        pieces.add(new King(Player.black, new Position(7, 4)));
        pieces.add(new Bishop(Player.black, new Position(7, 5)));
        pieces.add(new Knight(Player.black, new Position(7, 6)));
        pieces.add(new Rook(Player.black, new Position(7, 7)));

        ChessBoard chessBoard = Mockito.mock(ChessBoard.class);
        Mockito.when(chessBoard.getPieces()).thenReturn(pieces);
        Mockito.when(chessBoard.isSpaceTaken(Mockito.any())).thenReturn(false);
        Mockito.when(chessBoard.isSpaceTakenByOpposingPlayerPiece(Mockito.any(Position.class), Mockito.any(Player.class))).thenReturn(false);
        Mockito.when(chessBoard.isSpaceTakenByOpposingPlayerPiece(new Position(4, 5), Player.white)).thenReturn(true);
        double scoreBoard = fastScoringService.scoreBoard(chessBoard);
        Assertions.assertEquals(123, scoreBoard);
    }
}