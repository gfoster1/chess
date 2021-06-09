package com.whitespace.ai.movement;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;

import java.util.*;
import java.util.stream.Collectors;

public class DecisionTreeBestMoveService implements BestMoveService {
    private static final double losingScore = -200;
    private static final double winningScore = 200;

    private final Map<Integer, Long> heuristics = new HashMap<>();
    private final List<ExpectedOpponentMove> expectedOpponentMoves = new ArrayList<>();

    private final int maxDepth;
    private final Player player;
    private final Player opposingPlayer;
    private final BoardScoreService boardScoreService;


    public DecisionTreeBestMoveService(Player player, int maxDepth, BoardScoreService boardScoreService) {
        this.player = player;
        this.maxDepth = maxDepth;
        this.boardScoreService = boardScoreService;
        this.opposingPlayer = switch (player) {
            case white -> Player.black;
            case black -> Player.white;
        };
    }

    public Optional<Move> findBestMove(ChessBoard chessBoard) {

        ScoredMove bestMove;
        HashMap<Move, List<ScoredMove>> levelThreeMoves = new HashMap<>();
        if (expectedOpponentMoves.isEmpty()) {
            bestMove = findBestMove(chessBoard, 0, player, null, levelThreeMoves);
        } else {
            System.out.println("chessBoard = " + chessBoard);
//            System.out.println("expectedOpponentMove.finalScoredBoard = " + expectedOpponentMove.finalScoredBoard);
            bestMove = findBestMove(chessBoard, 0, player, null, new HashMap<>());
        }

        printHeuristics();
        heuristics.clear();
        System.out.println("bestMove = " + bestMove);
        return Optional.ofNullable(bestMove.move);
    }

    private ScoredMove findBestMove(ChessBoard chessBoard, int currentDepth, Player currentPlayer, Move originalMove, Map<Move, List<ScoredMove>> levelThreeMoves) {
        computeHeuristics(currentDepth);

        ScoredMove scoredMove;
        if (currentDepth == maxDepth) {
            scoredMove = maxDepthReached(chessBoard, originalMove);
        } else {
            double losingThreshHold = -50.0;
            double winningThreshHold = 50.0;

            ScoredMove maxScoredMove = new ScoredMove(losingThreshHold, originalMove);
            ScoredMove minScoredMove = new ScoredMove(winningThreshHold, originalMove);
            Set<Move> moves = chessBoard.getPossibleMoves(currentPlayer)
                    .collect(Collectors.toSet());

            for (Move move : moves) {
                var topLevelMove = originalMove == null ? move : originalMove;
                var moveResult = chessBoard.applyMove(move, false);

                ScoredMove currentScoredMove;
                if (moveResult.currentPlayerWins()) {
                    var score = currentPlayer.equals(player)
                            ? winningScore
                            : losingScore;
                    currentScoredMove = new ScoredMove(score, topLevelMove);
                } else if (moveResult.opponentWins()) {
                    var score = currentPlayer.equals(player)
                            ? losingScore
                            : winningScore;
                    currentScoredMove = new ScoredMove(score, topLevelMove);
                } else if (currentPlayer.equals(player) && chessBoard.isKingInCheck(player)) {
                    // this is a bad move
                    currentScoredMove = new ScoredMove(losingScore, topLevelMove);
//                } else if (currentPlayer.equals(opposingPlayer) && chessBoard.isKingInCheck(opposingPlayer)) {
//                    currentScoredMove = new ScoredMove(winningScore, topLevelMove);
                } else {
                    var nextPlayer = switch (currentPlayer) {
                        case white -> Player.black;
                        case black -> Player.white;
                    };
                    currentScoredMove = findBestMove(chessBoard, currentDepth + 1, nextPlayer, topLevelMove, levelThreeMoves);
                }

                if (currentScoredMove.score > maxScoredMove.score) {
                    maxScoredMove = currentScoredMove;
                }

                if (currentScoredMove.score < minScoredMove.score) {
                    minScoredMove = currentScoredMove;
                }

                if (originalMove == null) {
                    System.out.println("move = " + move + " minScoredMove= " + minScoredMove + " maxScoredMove= " + maxScoredMove + " hashcode = " + chessBoard.hashCode());
                }

                chessBoard.revertLastMove();
                long computationalLimits = 200000;
                if (heuristics.get(maxDepth) >= computationalLimits) {
                    break;
                }
            }
            scoredMove = currentPlayer.equals(player) ? maxScoredMove : minScoredMove;
        }
        return scoredMove;
    }

    private void printHeuristics() {
        heuristics.entrySet().stream().forEach(entry -> {
            String format = "level [%s] has [%s] calculations";
            String message = String.format(format, entry.getKey(), entry.getValue());
            System.out.println(message);
        });
    }

    private void computeHeuristics(int currentDepth) {
        heuristics.compute(currentDepth, (integer, value) -> {
            if (value == null) {
                return 1l;
            }
            return value + 1;
        });
    }


    private ScoredMove maxDepthReached(ChessBoard chessBoard, Move originalMove) {
        if (originalMove == null) {
            System.out.println("originalMove is null but at a depth!");
            return null;
        }

        double score = boardScoreService.scoreBoard(chessBoard);
        return new ScoredMove(score, originalMove);
    }

    private record ScoredMove(double score, Move move) {
    }

    private record ExpectedOpponentMove(ChessBoard originalChessBoard, ChessBoard finalScoredBoard) {
    }
}
