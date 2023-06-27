package com.foster.scoring;

import com.foster.BoardScoringService;
import com.foster.ChessBoard;
import com.foster.Player;
import com.foster.board.piece.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO move strength calculations into the scoring service
 * TODO add some fast caching for already seen boards
 */
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

    public double scoreBoard(ChessBoard board, Player player) {
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
    public double scorePieces(List<Piece> myPieces, List<Piece> opponentsPieces) {
        var myScore = scorePieces(myPieces);
        if (myScore == Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        var opponentScore = scorePieces(opponentsPieces);
        if (opponentScore == Integer.MIN_VALUE) {
            return Integer.MAX_VALUE;
        }
        return myScore - opponentScore;
    }

    private double scorePieces(List<Piece> myPieces) {
        var capturedMyKing = new AtomicBoolean(true);
        var numberOfSeenPieces = new HashMap<Class<?>, Integer>();
        var score = myPieces.stream()
                .peek(piece -> {
                    if (piece instanceof King) {
                        capturedMyKing.set(false);
                    }
                })
                .map(piece -> {
                    var baseStrength = rankRubrik.get(piece.getClass());

                    int row = piece.getPosition().row();
                    var positionalModifier = (row == 3 || row == 4) ? 2 : 1;

                    int knightModifier = 5;
                    int bishopModifier = 6;
                    int rookModifier = 7;
                    int numericModifier = switch (piece) {
                        case Knight knight ->
                                numberOfSeenPieces.compute(knight.getClass(), (clazz, integer) -> integer == null ? 0 : knightModifier);
                        case Bishop bishop ->
                                numberOfSeenPieces.compute(bishop.getClass(), (clazz, integer) -> integer == null ? 0 : bishopModifier);
                        case Rook rook ->
                                numberOfSeenPieces.compute(rook.getClass(), (clazz, integer) -> integer == null ? 0 : rookModifier);
                        default -> 0;
                    };
                    return (baseStrength * positionalModifier) + numericModifier;
                })
                .reduce(0, Integer::sum);

        if (capturedMyKing.get()) {
            score = Integer.MIN_VALUE;
        }

        return score;
    }
}
