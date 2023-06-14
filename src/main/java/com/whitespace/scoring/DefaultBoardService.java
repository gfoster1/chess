package com.whitespace.scoring;

import com.whitespace.BoardScoringService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Board;
import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultBoardService {
    private final Player player;
    private final BoardScoringService boardScoringService;

    public DefaultBoardService(Player player, BoardScoringService boardScoringService) {
        this.player = player;
        this.boardScoringService = boardScoringService;
    }

    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        HashMap<Move, List<Integer>> scores = new HashMap<>();
        findBestMove(chessBoard, 0, null, scores);
        if (scores.isEmpty()) {
            return Optional.empty();
        }
//        Move move = scores.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
//        return Optional.of(move);
        return Optional.empty();
    }

    private void findBestMove(ChessBoard board, int currentDepth, Move originalMove, Map<Move, List<Integer>> scores) {

        var maxDepth = 1;
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }
            System.out.println("We should never be here");
            return;
        }

        var myPieces =
                switch (player) {
                    case black -> board.getBlackPieces();
                    case white -> board.getWhitePieces();
                };
        myPieces.stream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(board).stream())
                .filter(move -> !board.isInvalidMove(move))
                .forEach(myMove -> {
                    board.applyMove(myMove, true);
                    // TODO add a check if this is a game winning move
                    var opponentsPieces = switch (player) {
                        case black -> board.getWhitePieces();
                        case white -> board.getBlackPieces();
                    };
                    opponentsPieces.stream()
                            .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(board).stream())
                            .filter(move -> !board.isInvalidMove(move))
                            .forEach(opponentsMove -> {
                                board.applyMove(opponentsMove, true);
                                Move move = originalMove == null ? myMove : originalMove;
                                if (currentDepth + 1 < maxDepth) {
                                    findBestMove(board, currentDepth + 1, move, scores);
                                } else {
                                    var moveScore = boardScoringService.scoreBoard(board, player);
                                    scores.compute(move, (m, integers) -> {
                                        if(integers == null){
                                            return new ArrayList<>();
                                        }
                                        return integers;
                                    }).add(moveScore);
                                }
                                board.revertLastMove();
                            });
                    board.revertLastMove();
                });
    }
}
