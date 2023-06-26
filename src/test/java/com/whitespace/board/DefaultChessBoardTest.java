package com.whitespace.board;

import com.whitespace.Player;
import com.whitespace.board.piece.Pawn;
import com.whitespace.board.piece.Piece;
import com.whitespace.board.piece.Queen;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DefaultChessBoardTest {

    @Test
    void testHashCode() {
//        assertEquals(new DefaultChessBoard(null, null).hashCode(),
//                new DefaultChessBoard(null, null).hashCode());
    }

    @Test
    public void testCopy() {
//        var d1 = new DefaultChessBoard(null, null);
//        var d2 = d1.copy();
//        assertTrue(d1.equals(d2));
    }

    @Test
    public void loadFromFEN() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null);

        List<Position> actualBlack = chessBoard.getBlackPieces().stream().map(piece -> piece.getPosition()).collect(Collectors.toList());
        Assertions.assertThat(actualBlack.size()).isEqualTo(16);
        List<Position> actualWhite = chessBoard.getWhitePieces().stream().map(piece -> piece.getPosition()).collect(Collectors.toList());
        Assertions.assertThat(actualWhite.size()).isEqualTo(16);

        chessBoard = new DefaultChessBoard(null, null, "rnbqkbnr/pppppppp/8/8/4PP/8/PPPP11PP/RNBQKBNR b KQkq e3 0 1");
        actualWhite = chessBoard.getWhitePieces().stream().map(piece -> piece.getPosition()).collect(Collectors.toList());
        Assertions.assertThat(actualWhite.size()).isEqualTo(16);
    }

    @Test
    public void applyMovePawnNoPromotionTranslateFen() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null);
        var piece = chessBoard.getWhitePieces().stream()
                .filter(p -> {
                    var position = p.getPosition();
                    return position.column() == 0 && position.row() == 6;
                })
                .findAny().get();
        var move = new Move(piece, new Position(4, 0));
        chessBoard.applyMove(move, true);
        String actual = chessBoard.translateToFEN();
        String expected = "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNBQKBNR - - - - -";
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void applyWhitePawnMoveTakeBlackNoPromotion() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null, "8/p7/8/8/8/8/P/8 b KQkq e3 0 1");
        var piece = chessBoard.getWhitePieces().get(0);
        var move = new Move(piece, new Position(1, 0));
        chessBoard.applyMove(move, true);
        Assertions.assertThat(chessBoard.getBlackPieces().size()).isEqualTo(0);
    }

    @Test
    public void applyWhitePawnMoveTakeBlackPromotion() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null, "p7/8/8/8/8/8/P/8 b KQkq e3 0 1");
        var piece = chessBoard.getWhitePieces().get(0);
        var move = new Move(piece, new Position(0, 0));
        chessBoard.applyMove(move, true);
        Assertions.assertThat(chessBoard.getWhitePieces().stream().filter(p -> p.getClass().equals(Queen.class)).collect(Collectors.toList()).size()).isEqualTo(1);
        Assertions.assertThat(chessBoard.getBlackPieces().size()).isEqualTo(0);
    }

    @Test
    public void startFromDefaultMovePawnRollbackSuccessful() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null);
        var piece = chessBoard.getWhitePieces().get(0);
        var move = new Move(piece, new Position(1, 0));
        chessBoard.applyMove(move, true);
        var expected = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR - - - - -";
        var actual = chessBoard.rollbackToPreviousMove().get();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void pawnMoveCaptureOpponentPromotedRollbackSuccessful() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null, "p7/8/8/8/8/8/P/8 b KQkq e3 0 1");
        var piece = chessBoard.getWhitePieces().get(0);
        var move = new Move(piece, new Position(0, 0));
        chessBoard.applyMove(move, true);
        var expected = "p7/8/8/8/8/8/P7/8 - - - - -";
        var actual = chessBoard.rollbackToPreviousMove().get();
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void startFromDefaultNoMoveRollbackNotPossible() {
        DefaultChessBoard chessBoard = new DefaultChessBoard(null, null);
        var expected = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR - - - - -";
        var actual = chessBoard.rollbackToPreviousMove().get();
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}