package com.whitespace.scoring;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoringService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBoardService implements BestMoveService {
    private final int maxDepth;
    private final Player player;
    private final BoardScoringService boardScoringService;

    private final Map<String, Double> cachedBoardScores = new HashMap<>();

    private final Map<String, ProcessedMoveNode> cachedProcessedMoveNodes = new HashMap<>();

    public DefaultBoardService(Player player, BoardScoringService boardScoringService, int maxDepth) {
        this.player = player;
        this.maxDepth = maxDepth;
        this.boardScoringService = boardScoringService;
    }

    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        var p = findBestMove(chessBoard, 1, null);
        return Optional.ofNullable(p.move);
    }

    private ProcessedMoveNode findBestMove(ChessBoard chessBoard, int currentDepth, ProcessedMoveNode originalNode) {
        if (currentDepth == maxDepth) {
            double score = getCachedMove(chessBoard, player);
            return new ProcessedMoveNode(originalNode.move, originalNode.FEN, score, false);
        }

        if (currentDepth > maxDepth) {
            throw new IllegalStateException("current depth is greater than max");
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
            var currentNode = cachedProcessedMoveNodes.computeIfAbsent(FEN, key -> new ProcessedMoveNode(myMove, key, Double.MIN_VALUE, false));
            if (currentNode.complete) {
                System.out.println("Joining nodes " + currentNode);
            } else {
                var opponentsPieces = switch (player) {
                    case black -> chessBoard.getWhitePieces();
                    case white -> chessBoard.getBlackPieces();
                };
                List<Move> opponentsMoves = opponentsPieces.parallelStream()
                        .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                        .collect(Collectors.toList());
                List<ProcessedMoveNode> processedOpponentMoves = new ArrayList<>(opponentsMoves.size());
                for (int j = 0; j < opponentsMoves.size(); j++) {
//                String format = "i = [%s] j = [%s] depth = [%s]";
//                String msg = String.format(format, i, j, currentDepth);
//                System.out.println(msg);
                    var opponentsMove = opponentsMoves.get(j);
                    chessBoard.applyMove(opponentsMove, false);
                    var processedOpponentsMove = findBestMove(chessBoard, currentDepth + 1, currentNode);
                    chessBoard.rollbackToPreviousMove(false);
                    processedOpponentMoves.add(processedOpponentsMove);
                }
                DoubleSummaryStatistics doubleSummaryStatistics = processedOpponentMoves.stream()
                        .mapToDouble(processedMoveNode -> processedMoveNode.score)
                        .summaryStatistics();
                currentNode.complete = true;
                currentNode.score = doubleSummaryStatistics.getAverage();
                cachedBoardScores.put(FEN, currentNode.score);
            }

            chessBoard.rollbackToPreviousMove(false);
            if (bestMove.move == null || currentNode.score > bestMove.score) {
                bestMove = currentNode;
                System.out.println(currentNode);
            }
        }
        System.out.println("The best move for the current board is = " + bestMove);
        return bestMove;
    }

    private double getCachedMove(ChessBoard chessBoard, Player player) {
        var FEN = chessBoard.translateToFEN();
        var score = cachedBoardScores.compute(FEN, (key, value) -> {
            var result = value;
            if (value == null) {
                System.out.println("Miss FEN = " + key);
                result = boardScoringService.scoreBoard(chessBoard, player);
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
        private Double score = Double.MIN_VALUE;
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
            String format = "piece = [%s] dest = [%s] score = [%s] FEN = [%s] ";
            return String.format(format, move.piece(), move.destination(), score, FEN);
        }
    }
}
