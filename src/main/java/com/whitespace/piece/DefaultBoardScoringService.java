package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.BoardScoringService;
import com.whitespace.Player;
import com.whitespace.movement.Move;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBoardScoringService implements BoardScoringService {
    private final Player player;

    public DefaultBoardScoringService(Player player) {
        this.player = player;
    }

    public Optional<Move> findBestMove(Board board) {
        HashMap<Move, Integer> scores = new HashMap<>();
        findBestMove(board, 0, null, scores);
        if(scores.isEmpty()){
            return Optional.empty();
        }
        Move move = scores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
        return Optional.of(move);
    }

    private void findBestMove(Board board, int currentDepth, Move originalMove, Map<Move, Integer> scores) {
        var moveScore = scoreBoard(board);
        int threshHold = -20;
        if (moveScore < threshHold) {
            return;
        }

        var maxDepth = 1;
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }

            scores.compute(originalMove, (move, integer) -> {
                if (integer == null || moveScore > integer) {
                    return moveScore;
                }
                return integer;
            });
            return;
        }

        var myMoves = switch (player) {
            case black -> getValidPossibleMoves(board.getBlackPieces(), board);
            case white -> getValidPossibleMoves(board.getWhitePieces(), board);
        };
        for (Move myMove : myMoves) {
            // get all possible opponent moves from this move
            board.applyMove(myMove, false);

            var opponentsMoves = switch (player) {
                case black -> getValidPossibleMoves(board.getWhitePieces(), board);
                case white -> getValidPossibleMoves(board.getBlackPieces(), board);
            };
            for (Move opponentsMove : opponentsMoves) {
                board.applyMove(opponentsMove, false);
                Move move = originalMove == null ? myMove : originalMove;
                findBestMove(board, currentDepth + 1, move, scores);
                board.revertLastMove();
            }
            board.revertLastMove();
        }
    }

    /**
     * @return 0 > Integer.MAX score of the board strength. If the score is Integer.MIN | Max than that side
     * won or lost.
     */
    private int scoreBoard(Board board) {
        var myKingCaptured = new AtomicBoolean(true);
        var opposingKingCaptured = new AtomicBoolean(true);
        int score = Stream.concat(board.getBlackPieces().stream(), board.getWhitePieces().stream())
                .map(piece -> {
                    int opposingPlayersTurn = -1;
                    int multiplier = opposingPlayersTurn;
                    if (piece.getPlayer().equals(player)) {
                        multiplier = 1;
                    }

                    if (piece instanceof King) {
                        if (multiplier == opposingPlayersTurn) {
                            opposingKingCaptured.set(false);
                        } else {
                            myKingCaptured.set(false);
                        }
                    }

                    int strength = piece.strength(board);
                    if (piece.getPosition().row() == 3 || piece.getPosition().row() == 4) {
                        int middleMultipler = 3;
                        strength += middleMultipler;
                    }
                    return strength * multiplier;
                })
                .reduce(0, Integer::sum);

        if (myKingCaptured.get()) {
            // you lost sucka
            score = Integer.MIN_VALUE;
        } else if (opposingKingCaptured.get()) {
            // winna winna chicken dinna
            score = Integer.MAX_VALUE;
        }
        return score;
    }

    private Set<Move> getValidPossibleMoves(List<Piece> pieces, Board board) {
        return pieces.stream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(board).stream())
                .filter(move -> !board.isInvalidMove(move))
                .collect(Collectors.toSet());
    }

    private record ScoredMove(int score, Move move) {

    }
}
