package com.whitespace.scoring;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoringService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class DefaultBoardService implements BestMoveService {
    private final Map<String, Integer> cachedBoardScores = new HashMap<>();
    private final int maxDepth;
    private final Player player;
    private final BoardScoringService boardScoringService;

    public DefaultBoardService(Player player, BoardScoringService boardScoringService, int maxDepth) {
        this.player = player;
        this.maxDepth = maxDepth;
        this.boardScoringService = boardScoringService;
    }

    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        Map<Move, List<Integer>> scores = new HashMap<>();
        findBestMove(chessBoard, 0, null, scores);
        if (scores.isEmpty()) {
            return Optional.empty();
        }
//        Move move = scores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
//        return Optional.of(move);
        return Optional.empty();
    }

    private double findBestMove(ChessBoard chessBoard, int currentDepth, Move originalMove, Map<Move, List<Integer>> scores) {

        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                throw new IllegalStateException("We have a problem");
            }

            var FEN = chessBoard.translateToFEN();
            var score = cachedBoardScores.compute(FEN, (key, value) -> {
                var result = value;
                if (value == null) {
                    System.out.println("Miss FEN = " + key);
                    result = boardScoringService.scoreBoard(chessBoard, originalMove.piece().getPlayer());
                } else {
                    System.out.println("Hit FEN = " + key);
                }
                return result;
            });
            scores.get(originalMove).add(score);
            return score;
        }

        var myPieces = switch (player) {
            case black -> chessBoard.getBlackPieces();
            case white -> chessBoard.getWhitePieces();
        };
        MoveProcessResults topResult = new MoveProcessResults(null, null, new DoubleSummaryStatistics());
        List<Move> myMoves = myPieces.parallelStream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                .collect(Collectors.toList());
        for (int i = 0; i < myMoves.size(); i++) {
            var myMove = myMoves.get(i);
            chessBoard.applyMove(myMove, false);
            var move = originalMove == null ? myMove : originalMove;
            if (originalMove == null) {
                scores.computeIfAbsent(move, m -> new ArrayList<>());
            }

            var opponentsPieces = switch (player) {
                case black -> chessBoard.getWhitePieces();
                case white -> chessBoard.getBlackPieces();
            };
            List<Double> opponentResults = new ArrayList<>();
            List<Move> opponentsMoves = opponentsPieces.parallelStream()
                    .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                    .collect(Collectors.toList());
            for (int j = 0; j < opponentsMoves.size(); j++) {
                var opponentsMove = opponentsMoves.get(j);
                chessBoard.applyMove(opponentsMove, false);
                var score = findBestMove(chessBoard, currentDepth + 1, move, scores);
                opponentResults.add(score);
                chessBoard.rollbackToPreviousMove();
            }
            var FEN = chessBoard.rollbackToPreviousMove().get();
            DoubleSummaryStatistics doubleSummaryStatistics = opponentResults.stream()
                    .mapToDouble(d -> d.doubleValue())
                    .summaryStatistics();
            if (doubleSummaryStatistics.getAverage() > topResult.doubleSummaryStatistics.getAverage()) {
                topResult = new MoveProcessResults(myMove, FEN, doubleSummaryStatistics);
            }
        }
        return topResult.doubleSummaryStatistics.getAverage();
    }

    private static final class MoveProcessResults {
        private Move move;
        private String FEN;
        private DoubleSummaryStatistics doubleSummaryStatistics = new DoubleSummaryStatistics();

        private boolean complete = false;

        public MoveProcessResults(Move move, String FEN, DoubleSummaryStatistics doubleSummaryStatistics) {
            this.move = move;
            this.FEN = FEN;
            this.doubleSummaryStatistics = doubleSummaryStatistics;
        }
    }
}
