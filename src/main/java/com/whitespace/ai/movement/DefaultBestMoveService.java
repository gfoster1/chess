package com.whitespace.ai.movement;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
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
        var scores = new HashMap<Move, DoubleStream.Builder>();
        findBestMove(chessBoard, 0, null, scores);
        List<ScoredMove> scoredMoves = scores.entrySet().parallelStream()
                .map(entry -> {
                    DoubleSummaryStatistics summaryStatistics = entry.getValue().build().summaryStatistics();
                    Move move = entry.getKey();
                    return new ScoredMove(move, summaryStatistics);
                })
                .filter(new Predicate<ScoredMove>() {
                    @Override
                    public boolean test(ScoredMove scoredMove) {
                        return scoredMove.doubleSummaryStatistics().getMin() > -20;
                    }
                })
                .sorted(new Comparator<ScoredMove>() {
                    @Override
                    public int compare(ScoredMove o1, ScoredMove o2) {
                        DoubleSummaryStatistics o1Stats = o1.doubleSummaryStatistics();
                        DoubleSummaryStatistics o2Stats = o2.doubleSummaryStatistics();
                        int compare = Double.compare(o2Stats.getMax(), o1Stats.getMax());
                        if (compare == 0) {
                            compare = Double.compare(o2Stats.getAverage(), o1Stats.getAverage());
                        }

                        if (compare == 0) {
                            compare = Double.compare(o2Stats.getMin(), o1Stats.getMin());
                        }
                        return compare;
                    }
                })
                .collect(Collectors.toList());
        var optimalMove = scoredMoves.isEmpty() ? null : scoredMoves.get(0).move();
        return Optional.ofNullable(optimalMove);
    }

    private void findBestMove(ChessBoard chessBoard, int currentDepth, Move originalMove, Map<Move, DoubleStream.Builder> scores) {
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }
            var score = boardScoreService.scoreBoard(chessBoard);
            scores.compute(originalMove,
                    (move, builder) -> {
                        DoubleStream.Builder b = builder;
                        if (builder == null) {
                            b = DoubleStream.builder();
                        }
                        return b;
                    })
                    .add(score);
            return;
        }

        var myMoves = chessBoard.getPieces().parallelStream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .collect(Collectors.toSet());

        for (Move myMove : myMoves) {
            chessBoard.applyMove(myMove, false);
//            var badMoveThreshHold = -10;
//            var myMoveNoBrainerThreshHold = 50;
//            var myMoveScore = boardScoreService.scoreBoard(chessBoard);
//            if (myMoveScore > myMoveNoBrainerThreshHold) {
//                scores.compute(originalMove, (move1, doubles) -> doubles == null ? new ArrayList<>() : doubles)
//                        .add(myMoveScore);
//                chessBoard.revertLastMove();
//                break;
//            }

//            if (myMoveScore > badMoveThreshHold) {
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
//            }
            chessBoard.revertLastMove();
        }
    }

    private record ScoredMove(Move move, DoubleSummaryStatistics doubleSummaryStatistics) {
    }
}
