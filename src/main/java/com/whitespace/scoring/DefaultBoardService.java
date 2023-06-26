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

    public DefaultBoardService(Player player, BoardScoringService boardScoringService, int maxDepth) {
        this.player = player;
        this.maxDepth = maxDepth;
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
        List<Move> moves = myPieces.parallelStream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
                .collect(Collectors.toList());
        for (int i = 0; i < moves.size(); i++) {
            var move = moves.get(i);
            chessBoard.applyMove(move, true);
            chessBoard.rollbackToPreviousMove();
        }

//        myPieces.parallelStream()
//                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
//                .forEach(myMove -> {
//                    chessBoard.applyMove(myMove, true);
//                    // TODO add a check if this is a game winning move
//                    var opponentsPieces = switch (player) {
//                        case black -> chessBoard.getWhitePieces();
//                        case white -> chessBoard.getBlackPieces();
//                    };
//                    opponentsPieces.stream()
//                            .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleStreamMoves(chessBoard))
//                            .forEach(opponentsMove -> {
//                                chessBoard.applyMove(opponentsMove, true);
//                                Move move = originalMove == null ? myMove : originalMove;
//                                if (currentDepth + 1 < maxDepth) {
//                                    findBestMove(chessBoard, currentDepth + 1, move, scores);
//                                } else {
//                                    var moveScore = boardScoringService.scoreBoard(chessBoard, player);
//                                    scores.compute(move, (m, integers) -> {
//                                        if (integers == null) {
//                                            return new ArrayList<>();
//                                        }
//                                        return integers;
//                                    }).add(moveScore);
//                                }
//                                chessBoard.rollbackToPreviousMove();
//                            });
//                    chessBoard.rollbackToPreviousMove();
//                });
    }
}
