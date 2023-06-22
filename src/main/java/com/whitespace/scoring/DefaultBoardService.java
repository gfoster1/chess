package com.whitespace.scoring;

import com.whitespace.BoardScoringService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.piece.Piece;

import java.util.*;
import java.util.function.Function;
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

    private void findBestMove(ChessBoard chessBoard, int currentDepth, Move originalMove, Map<Move, List<Integer>> scores) {

        var maxDepth = 1;
        if (currentDepth == maxDepth) {
            if (originalMove == null) {
                System.out.println("We have a problem");
                return;
            }
            System.out.println("We should never be here");
            return;
        }

        var myPieces = switch (player) {
            case black -> chessBoard.getBlackPieces();
            case white -> chessBoard.getWhitePieces();
        };
        myPieces.parallelStream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                .filter(move -> !chessBoard.isInvalidMove(move))
                .forEach(myMove -> {
                    chessBoard.applyMove(myMove, true);
                    // TODO add a check if this is a game winning move
                    var opponentsPieces = switch (player) {
                        case black -> chessBoard.getWhitePieces();
                        case white -> chessBoard.getBlackPieces();
                    };
                    opponentsPieces.stream()
                            .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).stream())
                            .filter(move -> !chessBoard.isInvalidMove(move))
                            .forEach(opponentsMove -> {
                                chessBoard.applyMove(opponentsMove, true);
                                Move move = originalMove == null ? myMove : originalMove;
                                if (currentDepth + 1 < maxDepth) {
                                    findBestMove(chessBoard, currentDepth + 1, move, scores);
                                } else {
                                    var moveScore = boardScoringService.scoreBoard(chessBoard, player);
                                    scores.compute(move, (m, integers) -> {
                                        if (integers == null) {
                                            return new ArrayList<>();
                                        }
                                        return integers;
                                    }).add(moveScore);
                                }
                                chessBoard.revertLastMove();
                            });
                    chessBoard.revertLastMove();
                });
    }
}
