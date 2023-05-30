package com.whitespace.ai.scoring;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.piece.*;

public class FastBoardScoringService implements BoardScoreService {
    private final Player player;
    private final int middleModifier;

    public FastBoardScoringService(Player player, int middleModifier) {
        this.player = player;
        this.middleModifier = middleModifier;
    }

    @Override
    public double scoreBoard(ChessBoard chessBoard) {
        StatefullMoveScorer statefullMoveScorer = new StatefullMoveScorer(player, middleModifier);
        chessBoard.getPieces().parallelStream().forEach(piece -> {
            var moves = piece.possibleMoves(chessBoard);
            if (piece instanceof Rook rook) {
                statefullMoveScorer.score(rook, moves);
            } else if (piece instanceof Bishop bishop) {
                statefullMoveScorer.score(bishop, moves);
            } else if (piece instanceof Knight knight) {
                statefullMoveScorer.score(knight, moves);
            } else if (piece instanceof Queen queen) {
                statefullMoveScorer.score(queen, moves);
            } else if (piece instanceof King king) {
                statefullMoveScorer.score(king, moves);
            } else if (piece instanceof Pawn pawn) {
                statefullMoveScorer.score(pawn, moves);
            } else {
                System.out.println("invalid piece " + piece);
            }
        });
        return statefullMoveScorer.calculateFinalScore();
    }
}
