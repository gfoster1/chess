package com.whitespace.scoring;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoringService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBoardService implements BestMoveService {
    private final int maxDepth;
    private final Player player;
    private final BoardScoringService boardScoringService;
    private final Set<ProcessedMoveNode> nodes = new HashSet<>();
    private final Map<String, Double> cachedBoardScores = new HashMap<>();

    private final Map<String, ProcessedMoveNode> cachedProcessedMoveNodes = new HashMap<>();

    public DefaultBoardService(Player player, BoardScoringService boardScoringService, int maxDepth) {
        this.player = player;
        this.maxDepth = maxDepth;
        this.boardScoringService = boardScoringService;
    }

    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        Map<Move, List<Integer>> scores = new HashMap<>();
        findBestMove(chessBoard, 0, null);
        if (scores.isEmpty()) {
            return Optional.empty();
        }
//        Move move = scores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
//        return Optional.of(move);
        return Optional.empty();
    }

    private ProcessedMoveNode findBestMove(ChessBoard chessBoard, int currentDepth, ProcessedMoveNode originalNode) {
        if (currentDepth == maxDepth) {
            double score = getCachedMove(chessBoard, player);
            return new ProcessedMoveNode(originalNode.move, originalNode.FEN, score, false);
        }

        var myPieces = switch (player) {
            case black -> chessBoard.getBlackPieces();
            case white -> chessBoard.getWhitePieces();
        };
        var myMoves = myPieces.parallelStream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                .collect(Collectors.toList());
        ProcessedMoveNode bestMove = new ProcessedMoveNode();
        for (int i = 0; i < myMoves.size(); i++) {
            var myMove = myMoves.get(i);
            String FEN = chessBoard.applyMove(myMove, true).get();
            var currentNode = new ProcessedMoveNode(myMove, FEN, Double.MIN_VALUE, false);

            var opponentsPieces = switch (player) {
                case black -> chessBoard.getWhitePieces();
                case white -> chessBoard.getBlackPieces();
            };
            List<Move> opponentsMoves = opponentsPieces.parallelStream()
                    .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                    .collect(Collectors.toList());
            List<ProcessedMoveNode> processedOpponentMoves = new ArrayList<>(opponentsMoves.size());
            for (int j = 0; j < opponentsMoves.size(); j++) {
                var opponentsMove = opponentsMoves.get(j);
                chessBoard.applyMove(opponentsMove, false);
                var processedOpponentsMove = findBestMove(chessBoard, currentDepth + 1, currentNode);
                processedOpponentMoves.add(processedOpponentsMove);
                chessBoard.rollbackToPreviousMove(false);
            }
            DoubleSummaryStatistics doubleSummaryStatistics = processedOpponentMoves.stream()
                    .mapToDouble(processedMoveNode -> processedMoveNode.score)
                    .summaryStatistics();
            currentNode.complete = true;
            currentNode.score = doubleSummaryStatistics.getAverage();
            if (currentNode.score > bestMove.score) {
                bestMove = currentNode;
            }
            chessBoard.rollbackToPreviousMove(false);
        }
//        Collections.sort(processedOpponentMoves, (p1, p2) -> Double.compare(p2.score, p1.score));
//        var bestMove = processedOpponentMoves.get(0);
        System.out.println("The best move for the current board is = " + bestMove);
        return bestMove;
    }

    private double getCachedMove(ChessBoard chessBoard, Player player) {
        var FEN = chessBoard.translateToFEN();
        var score = cachedBoardScores.compute(FEN, (key, value) -> {
            var result = value;
            if (value == null) {
                System.out.println("Miss FEN = " + key);
                result = (double) boardScoringService.scoreBoard(chessBoard, player);
            } else {
                System.out.println("Hit FEN = " + key);
            }
            return result;
        });
        return score;
    }

    private static final class ProcessedMoveNode {
        private Move move;
        private String FEN;
        //        private DoubleSummaryStatistics doubleSummaryStatistics = new DoubleSummaryStatistics();
        private double score = Double.MIN_VALUE;
        private boolean complete = false;

        public ProcessedMoveNode(Move move, String FEN, double score, boolean complete) {
            this.move = move;
            this.FEN = FEN;
            this.score = score;
            this.complete = complete;
        }

        public ProcessedMoveNode() {
        }

        @Override
        public String toString() {
            String format = "FEN = [%s] score = [%s]";
            return String.format(format, FEN, score);
        }
    }
}
