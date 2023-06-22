package com.whitespace.scoring;

import com.whitespace.BoardScoringService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;
import com.whitespace.board.piece.Pawn;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

class DefaultBoardServiceTest {


    @Test
    public void oneLevel() {
        BoardScoringService boardScoreService = Mockito.mock(BoardScoringService.class);
        DefaultBoardService defaultBoardService = new DefaultBoardService(Player.white, boardScoreService);
        Pawn blackPawn = new Pawn(Player.black, new Position(7,0));
        Pawn whitePawn = new Pawn(Player.white, new Position(1,0));

        ChessBoard board = Mockito.mock(ChessBoard.class);
        Mockito.when(board.getBlackPieces()).thenReturn(Collections.singletonList(blackPawn));
        Mockito.when(board.getWhitePieces()).thenReturn(Collections.singletonList(whitePawn));

        Mockito.when(boardScoreService.scorePieces(Mockito.anyList(), Mockito.anyList())).thenReturn(50);
        Mockito.when(boardScoreService.scoreBoard(Mockito.any(), Mockito.any())).thenReturn(50);
        Optional<Move> move = defaultBoardService.findBestMove(board);
    }
}