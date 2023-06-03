package com.whitespace.ai.movement;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.MoveResult;
import com.whitespace.board.piece.Piece;

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
        var scores = new HashMap<Move, List<Double>>();
        findBestMove(chessBoard, 0, null, scores);
        List<ScoredMove> scoredMoves = scores.entrySet().parallelStream()
                .map(entry -> {
                    DoubleSummaryStatistics summaryStatistics = entry.getValue().stream()
                            .mapToDouble(value -> value.doubleValue())
                            .summaryStatistics();
                    double variance = entry.getValue().parallelStream()
                            .mapToDouble(value -> value.doubleValue())
                            .map(operand -> Math.pow(operand - summaryStatistics.getAverage(), 2))
                            .sum() / summaryStatistics.getCount() - 1;
                    Move move = entry.getKey();
                    return new ScoredMove(move, summaryStatistics, variance);
                })
                .filter(scoredMove -> {
                    int lossThreshHold = -100;
                    return scoredMove.doubleSummaryStatistics().getMin() > lossThreshHold;
                })
                .sorted((o1, o2) -> {
                    DoubleSummaryStatistics o1Stats = o1.doubleSummaryStatistics();
                    DoubleSummaryStatistics o2Stats = o2.doubleSummaryStatistics();
                    double d1 = (o1Stats.getMax() + o1Stats.getAverage()) / Math.sqrt(o1.variance);
                    double d2 = (o1Stats.getMax() + o2Stats.getAverage()) / Math.sqrt(o2.variance);
                    int compare = Double.compare(d2, d1);
                    if (compare == 0) {
                        compare = Double.compare(o2Stats.getMax(), o1Stats.getMax());
                    }

                    if (compare == 0) {
                        compare = Double.compare(o1Stats.getMin(), o2Stats.getMin());
                    }
                    return compare;
                })
                .collect(Collectors.toList());
        var optimalMove = scoredMoves.isEmpty() ? null : scoredMoves.get(0).move();
        return Optional.ofNullable(optimalMove);
    }

    private void findBestMove(ChessBoard chessBoard, int currentDepth, Move originalMove, Map<Move, List<Double>> scores) {
//        int maxScoresPerMove = 1000;
//        if (originalMove != null && scores.get(originalMove).size() >= maxScoresPerMove) {
//            return;
//        }
//
//        if (currentDepth == maxDepth) {
//            if (originalMove == null) {
//                System.out.println("We have a problem");
//                return;
//            }
//
//            scores.get(originalMove).add(boardScoreService.scoreBoard(chessBoard));
//            return;
//        }
//
//        var myMoves = chessBoard.getPieces().parallelStream()
//                .filter(piece -> piece.getPlayer().equals(player))
//                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
//                .collect(Collectors.toSet());
//
//        for (Move myMove : myMoves) {
//            if (currentDepth == 0) {
//                scores.put(myMove, new ArrayList<>(maxScoresPerMove));
//            }
//
//            double winValue = 200 / (currentDepth + 1);
//            double loseValue = -200 / (currentDepth + 1);
//            var move = originalMove == null ? myMove : originalMove;
//            MoveResult myMoveResult = chessBoard.applyMove(myMove, false);
//            if (myMoveResult.opponentWins()) {
//                scores.get(move).add(loseValue);
//                chessBoard.revertLastMove();
//                break;
//            }
//
//            if (myMoveResult.currentPlayerWins()) {
//                scores.get(move).add(winValue);
//                chessBoard.revertLastMove();
//                break;
//            }
//
//            var opponentsMoves = chessBoard.getPieces().parallelStream()
//                    .filter(piece -> !piece.getPlayer().equals(player))
//                    .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
//                    .collect(Collectors.toSet());
//
//            winValue = 200 / (currentDepth + 2);
//            loseValue = -200 / (currentDepth + 2);
//            for (Move opponentsMove : opponentsMoves) {
//                MoveResult opponentMoveResult = chessBoard.applyMove(opponentsMove, false);
//                if (opponentMoveResult.currentPlayerWins()) {
//                    // bad bad move
//                    scores.get(move).add(loseValue);
//                    chessBoard.revertLastMove();
//                    break;
//                }
//
//                if (opponentMoveResult.opponentWins()) {
//                    // sweet move dude
//                    scores.get(move).add(winValue);
//                    chessBoard.revertLastMove();
//                    break;
//                }
//
//                findBestMove(chessBoard, currentDepth + 1, move, scores);
//                chessBoard.revertLastMove();
//            }
//            chessBoard.revertLastMove();
//        }
    }

    private record ScoredMove(Move move, DoubleSummaryStatistics doubleSummaryStatistics, double variance) {
    }
}
