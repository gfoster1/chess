package com.whitespace.scoring;

import com.whitespace.Board;
import com.whitespace.BoardScoringService;
import com.whitespace.BoardService;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.piece.King;
import com.whitespace.piece.Piece;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBoardService implements BoardService {
    private final Player player;
    private final BoardScoringService boardScoringService;

    public DefaultBoardService(Player player, BoardScoringService boardScoringService) {
        this.player = player;
        this.boardScoringService = boardScoringService;
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
        var moveScore = boardScoringService.scoreBoard(board, player);
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

    private Set<Move> getValidPossibleMoves(List<Piece> pieces, Board board) {
        return pieces.stream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(board).stream())
                .filter(move -> !board.isInvalidMove(move))
                .collect(Collectors.toSet());
    }

    private record ScoredMove(int score, Move move) {

    }
}
