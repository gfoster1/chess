package com.whitespace.ai;

import com.whitespace.*;
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

    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        Map<Move, Double> scores = new HashMap<>();
        findBestMove(chessBoard, 0, null, scores);
        if (scores.isEmpty()) {
            return Optional.empty();
        }

        Move move = scores.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
        return Optional.of(move);
    }

    private void findBestMove(ChessBoard chessBoard, int currentDepth, Move originalMove, Map<Move, Double> scores) {
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }
            return;
        }

        if (originalMove != null) {
            // check if the thresh holds were triggered
            Double value = scores.getOrDefault(originalMove, 0d);
            if (Double.MIN_VALUE == value || Double.MAX_VALUE == value) {
                System.out.println("Thresh hold triggered on originalMove = " + originalMove);
                return;
            }
        }

        var myMoves = chessBoard.getPieces().parallelStream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .collect(Collectors.toSet());

        for (Move myMove : myMoves) {
            var move = originalMove == null ? myMove : originalMove;

            chessBoard.applyMove(myMove, false);
            var myMoveLowerThreshHold = -20;
            var myMoveNoBrainerThreshHold = 20;
            var myMoveScore = boardScoreService.scoreBoard(chessBoard);
            if (myMoveScore >= myMoveNoBrainerThreshHold) {
                // this move is so good it is a no brainer to execute
                System.out.println("Found a no brainer move = " + move);
                scores.put(move, Double.MAX_VALUE);
                chessBoard.revertLastMove();
                break;
            } else if (myMoveScore > myMoveLowerThreshHold) {
                // make sure I don't screw up here
                // get all possible opponent moves from this move
                var opponentsMoves = chessBoard.getPieces().parallelStream()
                        .filter(piece -> !piece.getPlayer().equals(player))
                        .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                        .collect(Collectors.toSet());

                for (Move opponentsMove : opponentsMoves) {
                    chessBoard.applyMove(opponentsMove, false);
                    processOpponentMove(chessBoard, currentDepth, scores, move);
                    chessBoard.revertLastMove();
                }
            }
            chessBoard.revertLastMove();
        }
    }

    private void processOpponentMove(ChessBoard chessBoard, int currentDepth, Map<Move, Double> scores, Move move) {
        double opponentMoveThreshHold = -20;
        var opponentMoveScore = boardScoreService.scoreBoard(chessBoard);
        if (opponentMoveScore > opponentMoveThreshHold) {
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
            findBestMove(chessBoard, currentDepth + 1, move, scores);
        }
    }

}
