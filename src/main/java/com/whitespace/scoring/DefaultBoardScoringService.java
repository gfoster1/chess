package com.whitespace.scoring;

import com.whitespace.Board;
import com.whitespace.BoardScoringService;
import com.whitespace.Player;
import com.whitespace.piece.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultBoardScoringService implements BoardScoringService {
    private final Map<Class<?>, Integer> rankRubrik = new HashMap<>();

    public DefaultBoardScoringService() {
        rankRubrik.put(King.class, 100);
        rankRubrik.put(Queen.class, 50);
        rankRubrik.put(Bishop.class, 80);
        rankRubrik.put(Knight.class, 40);
        rankRubrik.put(Rook.class, 20);
        rankRubrik.put(Pawn.class, 1);
    }

    public int scoreBoard(Board board, Player player) {
        List<Piece> myPieces;
        List<Piece> opponentsPieces;
        if (player.equals(Player.black)) {
            myPieces = board.getBlackPieces();
            opponentsPieces = board.getWhitePieces();
        } else {
            myPieces = board.getWhitePieces();
            opponentsPieces = board.getBlackPieces();
        }
        return scorePieces(myPieces, opponentsPieces);
    }

    @Override
    public int scorePieces(List<Piece> myPieces, List<Piece> opponentsPieces) {
        int myScore = scoreMyPieces(myPieces);
        int opponentScore = scoreMyPieces(opponentsPieces);
        if (myScore == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        if (opponentScore == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }
        return myScore - opponentScore;
    }

    private int scoreMyPieces(List<Piece> myPieces) {
        var capturedMyKing = new AtomicBoolean(true);
        var score = myPieces.stream()
                .peek(piece -> {
                    if (piece instanceof King) {
                        capturedMyKing.set(false);
                    }
                })
                .map(piece -> {
                    var baseStrength = rankRubrik.get(piece.getClass());
                    var positionalModifier = 1;
                    var position = piece.getPosition();
                    if (position.row() == 3 || position.row() == 4) {
                        positionalModifier = 2;
                    }
                    return baseStrength * positionalModifier;
                })
                .reduce(0, Integer::sum);

        if (capturedMyKing.get()) {
            score = Integer.MIN_VALUE;
        }

        return score;
    }
}
