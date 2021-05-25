package com.whitespace.ai.movement;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.MoveResult;

import java.util.*;
import java.util.stream.Collectors;

public class DecisionTreeBestMoveService implements BestMoveService {
    private final Player player;
    private final int maxDepth;
    private final BoardScoreService boardScoreService;

    public DecisionTreeBestMoveService(Player player, int maxDepth, BoardScoreService boardScoreService) {
        this.player = player;
        this.maxDepth = maxDepth;
        this.boardScoreService = boardScoreService;
    }

    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        ScoredMove bestMove = secondTry(chessBoard, 0, player, null, null);
        return Optional.ofNullable(bestMove.move);
    }

    private ScoredMove secondTry(ChessBoard chessBoard, int currentDepth, Player currentPlayer, MoveResult originalMoveResult, Move originalMove) {
        if (currentDepth == maxDepth) {
            return maxDepthReached(chessBoard, originalMoveResult, originalMove);
        }

        Set<Move> moves = chessBoard.getPossibleMoves(currentPlayer)
                .collect(Collectors.toSet());
        Set<ScoredMove> scoredMoves = new HashSet<>(moves.size());
        var nextPlayer = switch (currentPlayer) {
            case white -> Player.black;
            case black -> Player.white;
        };
        for (Move move : moves) {
            var topLevelMove = originalMove == null
                    ? move
                    : originalMove;
            MoveResult moveResult = chessBoard.applyMove(move, false);
            ScoredMove scoredMove = secondTry(chessBoard, currentDepth + 1, nextPlayer, moveResult, topLevelMove);
            scoredMoves.add(scoredMove);
            chessBoard.revertLastMove();
        }

        var comparator = currentPlayer.equals(player)
                ? new Comparator<ScoredMove>() {
            @Override
            public int compare(ScoredMove o1, ScoredMove o2) {
                // max
                return Double.compare(o2.score, o1.score);
            }
        }
                : new Comparator<ScoredMove>() {
            @Override
            public int compare(ScoredMove o1, ScoredMove o2) {
                // min
                return Double.compare(o1.score, o2.score);
            }
        };

        ScoredMove scoredMove = scoredMoves.stream().sorted(comparator).findFirst().get();
        return scoredMove;
    }

    private ScoredMove maxDepthReached(ChessBoard chessBoard, MoveResult originalMoveResult, Move originalMove) {
        if (originalMoveResult == null) {
            System.out.println("originalMoveResult is null but at a depth!");
            return null;
        }

        if (originalMove == null) {
            System.out.println("originalMove is null but at a depth!");
            return null;
        }

        double score;
        if (originalMoveResult.opponentWins()) {
            score = -100;
        } else if (originalMoveResult.currentPlayerWins()) {
            score = 100;
        } else {
            score = boardScoreService.scoreBoard(chessBoard);
        }
        return new ScoredMove(score, originalMove);
    }

    private record ScoredMove(double score, Move move) {
    }
}