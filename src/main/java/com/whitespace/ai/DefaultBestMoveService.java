package com.whitespace.ai;

import com.whitespace.BestMoveService;
import com.whitespace.Board;
import com.whitespace.BoardScoreService;
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

    public Optional<Move> findBestMove(Board board) {
        HashMap<Move, Integer> scores = new HashMap<>();
        findBestMove(board, 0, null, scores);
        if (scores.isEmpty()) {
            return Optional.empty();
        }
        Move move = scores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
        return Optional.of(move);
    }

    private void findBestMove(Board board, int currentDepth, Move originalMove, Map<Move, Integer> scores) {
        var moveScore = boardScoreService.scoreBoard(board);
        int threshHold = -20;
        if (moveScore < threshHold) {
            return;
        }

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

        var myMoves = board.getPieces().stream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(board).stream())
                .collect(Collectors.toSet());

        for (Move myMove : myMoves) {
            // get all possible opponent moves from this move
            board.applyMove(myMove, false);

            var opponentsMoves = board.getPieces().stream()
                    .filter(piece -> !piece.getPlayer().equals(player))
                    .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(board).stream())
                    .collect(Collectors.toSet());

            for (Move opponentsMove : opponentsMoves) {
                board.applyMove(opponentsMove, false);
                var opponentScore = boardScoreService.scoreBoard(board);
                if (opponentScore > threshHold) {
                    Move move = originalMove == null ? myMove : originalMove;
                    findBestMove(board, currentDepth + 1, move, scores);
                }
                board.revertLastMove();
            }
            board.revertLastMove();
        }
    }
}
