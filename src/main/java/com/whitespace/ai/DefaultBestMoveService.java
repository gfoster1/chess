package com.whitespace.ai;

import com.whitespace.BestMoveService;
import com.whitespace.DefaultChessBoard;
import com.whitespace.BoardScoreService;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.piece.Piece;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBestMoveService implements BestMoveService {
    private final Player player;
    private final int maxDepth;
    private final BoardScoreService boardScoreService;

    public DefaultBestMoveService(Player player, int maxDepth, BoardScoreService boardScoreService) {
        this.player = player;
        this.maxDepth = maxDepth;
        this.boardScoreService = boardScoreService;
    }

    public Optional<Move> findBestMove(DefaultChessBoard defaultChessBoard) {
        Map<Move, Double> scores = new HashMap<>();
        findBestMove(defaultChessBoard, 0, null, scores);
        if (scores.isEmpty()) {
            return Optional.empty();
        }
        Move move = scores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
        return Optional.of(move);
    }

    private void findBestMove(DefaultChessBoard defaultChessBoard, int currentDepth, Move originalMove, Map<Move, Double> scores) {
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }
            return;
        }

        if (originalMove != null && Double.MIN_VALUE == scores.getOrDefault(originalMove, 0d)) {
            // check if the threshhold was triggered
            System.out.println("Thresh hold triggered on originalMove = " + originalMove);
            return;
        }

        var myMoves = defaultChessBoard.getPieces().parallelStream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(defaultChessBoard).parallelStream())
                .collect(Collectors.toSet());

        for (Move myMove : myMoves) {
            var move = originalMove == null ? myMove : originalMove;
            defaultChessBoard.applyMove(myMove, false);

            double threshHold = -20;
            var myMoveScore = boardScoreService.scoreBoard(defaultChessBoard);
            if (myMoveScore > threshHold) {
                // make sure I don't screw up here
                // get all possible opponent moves from this move
                var opponentsMoves = defaultChessBoard.getPieces().parallelStream()
                        .filter(piece -> !piece.getPlayer().equals(player))
                        .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(defaultChessBoard).parallelStream())
                        .collect(Collectors.toSet());

                for (Move opponentsMove : opponentsMoves) {
                    processOpponentMove(defaultChessBoard, currentDepth, scores, move, threshHold, opponentsMove);
                }
            }
            defaultChessBoard.revertLastMove();
        }
    }

    private void processOpponentMove(DefaultChessBoard defaultChessBoard, int currentDepth, Map<Move, Double> scores, Move move, double threshHold, Move opponentsMove) {
        defaultChessBoard.applyMove(opponentsMove, false);
        var opponentMoveScore = boardScoreService.scoreBoard(defaultChessBoard);
        if (opponentMoveScore > threshHold) {
            // loss thresh hold triggered
            scores.compute(move, (m1, existingScore) -> {
                if (existingScore == null) {
                    return opponentMoveScore;
                }
                if (opponentMoveScore > existingScore && existingScore != Double.MIN_VALUE) {
                    return opponentMoveScore;
                }
                return existingScore;
            });
            findBestMove(defaultChessBoard, currentDepth + 1, move, scores);
        }
        defaultChessBoard.revertLastMove();
    }
}
