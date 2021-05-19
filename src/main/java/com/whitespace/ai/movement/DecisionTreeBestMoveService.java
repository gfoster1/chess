package com.whitespace.ai.movement;

import com.whitespace.BestMoveService;
import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.MoveResult;
import com.whitespace.board.piece.Piece;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        ScoredMove bestMove = findBestMove(chessBoard, 0, player, null, new ConcurrentHashMap<>());
        return Optional.ofNullable(bestMove.move);
    }

    private ScoredMove findBestMove(ChessBoard chessBoard, int currentDepth, Player currentPlayer, Move originalMove, Map<Move, Long> threshHold) {
        if (currentDepth == maxDepth) {
            threshHold.computeIfPresent(originalMove, (key, value) -> {
                if (value == null) {
                    return 0l;
                }
                return value + 1;
            });
            double score = boardScoreService.scoreBoard(chessBoard);
            return new ScoredMove(score, originalMove);
        }

        var moves = chessBoard.getPieces().parallelStream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .collect(Collectors.toSet());

        double losingScore = -100;
        double winningScore = 100;
        if (moves.isEmpty()) {
            return new ScoredMove(losingScore, null);
        }

        ScoredMove bestMoveForMe = new ScoredMove(losingScore, originalMove);
        for (Move myMove : moves) {
            Move scoringMove;
            if (originalMove == null) {
                // first time calling this thing
                scoringMove = myMove;
                threshHold.put(scoringMove, 0l);
            } else {
                scoringMove = originalMove;
            }

            if (bestMoveForMe.score >= winningScore) {
                break;
            }

            long maxScoresPerScoringMove = 5000;
            var moveResult = chessBoard.applyMove(myMove, false);
            if (moveResult.currentPlayerWins()) {
                bestMoveForMe = new ScoredMove(winningScore, scoringMove);
            } else if (moveResult.opponentWins()) {
                // don't do anything
//                bestMoveForOpponent = new ScoredMove(losingScore, scoringMove);
            } else if (threshHold.get(scoringMove) <= maxScoresPerScoringMove) {
                ScoredMove bestMoveForOpponent = new ScoredMove(winningScore, scoringMove);
                Player opponentPlayer = switch (currentPlayer) {
                    case black -> Player.white;
                    case white -> Player.black;
                };
                var opponentMoves = chessBoard.getPieces().parallelStream()
                        .filter(piece -> piece.getPlayer().equals(opponentPlayer))
                        .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                        .collect(Collectors.toSet());
                for (Move opponentMove : opponentMoves) {
                    if (bestMoveForOpponent.score <= losingScore) {
                        break;
                    }

                    MoveResult opponentMoveResult = chessBoard.applyMove(opponentMove, false);
                    if (opponentMoveResult.currentPlayerWins()) {
                        // no good for the scoring player
                        bestMoveForOpponent = new ScoredMove(losingScore, scoringMove);
                    } else if (opponentMoveResult.opponentWins()) {
                        // good for scoring player
                    } else if (threshHold.get(scoringMove) <= maxScoresPerScoringMove) {
                        ScoredMove scoredMove = findBestMove(chessBoard,
                                currentDepth + 1,
                                opponentPlayer,
                                scoringMove,
                                threshHold);

                        if (bestMoveForOpponent.score > scoredMove.score) {
                            bestMoveForOpponent = scoredMove;
                        }
                    }
                    chessBoard.revertLastMove();
                }

                if (bestMoveForMe.score < bestMoveForOpponent.score) {
                    bestMoveForMe = bestMoveForOpponent;
                }
            }

            chessBoard.revertLastMove();
        }
        return bestMoveForMe;
    }

    private record ScoredMove(double score, Move move) {
    }
}