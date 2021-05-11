package com.whitespace.ai;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.piece.Piece;

import java.util.*;
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
        Move optimalMove = null;
        Map<Move, List<Double>> scores = new HashMap<>();
        findBestMove(chessBoard, 0, null, scores);
        if (!scores.isEmpty()) {
            Map<Move, DoubleSummaryStatistics> stuff = new HashMap<>();
            scores.entrySet().parallelStream().forEach(entry -> {
                DoubleSummaryStatistics doubleSummaryStatistics = entry.getValue().parallelStream().mapToDouble(value -> value).summaryStatistics();
                stuff.put(entry.getKey(), doubleSummaryStatistics);
            });
            Map.Entry<Move, DoubleSummaryStatistics> entry = stuff.entrySet().parallelStream()
                    .max((o1, o2) -> {
                        var d1 = o1.getValue().getAverage();
                        var d2 = o2.getValue().getAverage();
                        return Double.compare(d1, d2);
                    })
                    .stream().findFirst().get();
            System.out.println("entry = " + entry.getKey());
            System.out.println("entry = " + entry.getValue());
            optimalMove = entry.getKey();
        }
        return Optional.ofNullable(optimalMove);
    }

    private void findBestMove(ChessBoard chessBoard, int currentDepth, Move originalMove, Map<Move, List<Double>> scores) {
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }

            var score = boardScoreService.scoreBoard(chessBoard);
            scores.compute(originalMove, (move1, doubles) -> doubles == null ? new ArrayList<>() : doubles)
                    .add(score);
            return;
        }

        var myMoves = chessBoard.getPieces().parallelStream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .collect(Collectors.toSet());

        for (Move myMove : myMoves) {
            chessBoard.applyMove(myMove, false);
            var badMoveThreshHold = -10;
            var myMoveNoBrainerThreshHold = 50;
            var myMoveScore = boardScoreService.scoreBoard(chessBoard);
            if (myMoveScore > myMoveNoBrainerThreshHold) {
                scores.compute(originalMove, (move1, doubles) -> doubles == null ? new ArrayList<>() : doubles)
                        .add(myMoveScore);
                chessBoard.revertLastMove();
                break;
            }

            if (myMoveScore > badMoveThreshHold) {
                var opponentsMoves = chessBoard.getPieces().parallelStream()
                        .filter(piece -> !piece.getPlayer().equals(player))
                        .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                        .collect(Collectors.toSet());

                var move = originalMove == null ? myMove : originalMove;
                for (Move opponentsMove : opponentsMoves) {
                    chessBoard.applyMove(opponentsMove, false);
                    findBestMove(chessBoard, currentDepth + 1, move, scores);
                    chessBoard.revertLastMove();
                }
            }
            chessBoard.revertLastMove();
        }
    }

}
