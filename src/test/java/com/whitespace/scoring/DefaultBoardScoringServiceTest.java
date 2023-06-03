package com.whitespace.scoring;

import com.whitespace.Player;
import com.whitespace.board.Position;
import com.whitespace.board.piece.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBoardScoringServiceTest {
    DefaultBoardScoringService boardScoringService = new DefaultBoardScoringService();

    @Test
    void noMyKings() {
        List<Piece> myPieces = Arrays.asList(
                new Bishop(Player.white, new Position(0, 0))
        );
        List<Piece> opponentsPieces = Arrays.asList(
                new King(Player.black, new Position(1, 0))
        );
        var score = boardScoringService.scorePieces(myPieces, opponentsPieces);
        assertEquals(Integer.MIN_VALUE, score);
    }

    @Test
    void noOpponentKings() {
        List<Piece> myPieces = Arrays.asList(
                new King(Player.white, new Position(0, 0))
        );
        List<Piece> opponentsPieces = Arrays.asList(
                new Bishop(Player.black, new Position(1, 0))
        );
        var score = boardScoringService.scorePieces(myPieces, opponentsPieces);
        assertEquals(Integer.MAX_VALUE, score);
    }

    @Test
    void middleMultipler() {
        List<Piece> myPieces = Arrays.asList(
                new King(Player.white, new Position(3, 0))
        );
        List<Piece> opponentsPieces = Arrays.asList(
                new King(Player.black, new Position(1, 0))
        );
        var score = boardScoringService.scorePieces(myPieces, opponentsPieces);
        assertEquals(100, score);
    }

    @Test
    void testRubrik() {
        List<Piece> myPieces = Arrays.asList(
                new King(Player.white, new Position(1, 0)),
                new Queen(Player.white, new Position(1, 0)),
                new Bishop(Player.white, new Position(1, 0)),
                new Pawn(Player.white, new Position(1, 0)),
                new Rook(Player.white, new Position(1, 0)),
                new Knight(Player.white, new Position(1, 0))
        );
        List<Piece> opponentsPieces = Arrays.asList(
                new King(Player.black, new Position(1, 0))
        );
        var score = boardScoringService.scorePieces(myPieces, opponentsPieces);
        assertEquals(191, score);
    }

    @Test
    void testMultiplePieces() {
        List<Piece> myPieces = Arrays.asList(
                new King(Player.white, new Position(1, 0)),
                new Bishop(Player.white, new Position(1, 0)),
                new Bishop(Player.white, new Position(1, 0)),
                new Knight(Player.white, new Position(1, 0)),
                new Knight(Player.white, new Position(1, 0)),
                new Rook(Player.white, new Position(1, 0)),
                new Rook(Player.white, new Position(1, 0)),
                new Queen(Player.white, new Position(1, 0))
        );
        List<Piece> opponentsPieces = Arrays.asList(
                new King(Player.black, new Position(1, 0))
        );
        var score = boardScoringService.scorePieces(myPieces, opponentsPieces);
        assertEquals(348, score);
    }
}