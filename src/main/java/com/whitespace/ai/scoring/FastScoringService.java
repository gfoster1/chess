package com.whitespace.ai.scoring;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.piece.*;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FastScoringService implements BoardScoreService {
    private final Player player;
    private final int middleModifier;

    public FastScoringService(Player player, int middleModifier) {
        this.player = player;
        this.middleModifier = middleModifier;
    }

    @Override
    public double scoreBoard(ChessBoard chessBoard) {
        AwesomeCustomMoveScorer awesomeCustomMoveScorer = new AwesomeCustomMoveScorer(player, middleModifier);
        return chessBoard.getPieces().parallelStream()
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .collect(Collectors.groupingBy(move -> move.piece()))
                .entrySet().parallelStream()
                .map(entry -> {
                    var piece = entry.getKey();
                    var moves = entry.getValue();

                    var score = 0.0d;
                    if (piece instanceof Rook rook) {
                        score = awesomeCustomMoveScorer.score(rook, moves);
                    } else if (piece instanceof Bishop bishop) {
                        score = awesomeCustomMoveScorer.score(bishop, moves);
                    } else if (piece instanceof Knight knight) {
                        score = awesomeCustomMoveScorer.score(knight, moves);
                    } else if (piece instanceof Queen queen) {
                        score = awesomeCustomMoveScorer.score(queen, moves);
                    } else if (piece instanceof King king) {
                        score = awesomeCustomMoveScorer.score(king, moves);
                    } else if (piece instanceof Pawn pawn) {
                        score = awesomeCustomMoveScorer.score(pawn, moves);
                    }
                    return score;
                })
                .collect(Collectors.reducing(Double::sum))
                .orElse(0.0);
    }
}
