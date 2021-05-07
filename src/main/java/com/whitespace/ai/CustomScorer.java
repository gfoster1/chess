package com.whitespace.ai;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.movement.Position;
import com.whitespace.piece.*;

import java.util.*;

public class CustomScorer {
    private static final Map<Integer, Integer> blackValues = new HashMap<>();
    private static final Map<Integer, Integer> whiteValues = new HashMap<>();

    static {
        int queensStrength = 10;
        whiteValues.put(0, 1);
        whiteValues.put(1, 1);
        whiteValues.put(2, 2);
        whiteValues.put(3, 3);
        whiteValues.put(4, 4);
        whiteValues.put(5, 5);
        whiteValues.put(6, 6);
        whiteValues.put(7, queensStrength);

        blackValues.put(7, 1);
        blackValues.put(6, 1);
        blackValues.put(5, 2);
        blackValues.put(4, 3);
        blackValues.put(3, 4);
        blackValues.put(2, 5);
        blackValues.put(1, 6);
        blackValues.put(0, queensStrength);
    }

    private final Player player;
    private final int middleModifier;

    private final List<Rook> myRooks = new ArrayList<>(2);
    private final List<Rook> opponentsRooks = new ArrayList<>(2);
    private int myKnights = 0;
    private int myBishops = 0;
    private int opponentsBishops = 0;
    private int opponentsKnights = 0;
    private double totalScore = 0;

    public CustomScorer(Player player, int middleModifier) {
        this.player = player;
        this.middleModifier = middleModifier;
    }

    public void reset() {
        myRooks.clear();
        opponentsRooks.clear();
        myKnights = 0;
        myBishops = 0;
        opponentsBishops = 0;
        opponentsKnights = 0;
        totalScore = 0;
    }

    protected void score(Rook rook) {
        var opennessModifier = 1.0;
//        int size = rook.possibleMoves(chessBoard).size();
//        if (size > 6) {
//            opennessModifier = 1.1;
//        } else if (size < 4) {
//            opennessModifier = .9;
//        }

        var twoRookModifier = 0.0;
        if (rook.getPlayer().equals(player)) {
            myRooks.add(rook);
            if (myRooks.size() == 2) {
                twoRookModifier = .5;
            }
        } else {
            opponentsRooks.add(rook);
            if (opponentsRooks.size() == 2) {
                twoRookModifier = .5;
            }

        }
        var positionalStrength = computePositionalStrengthScore(rook.getPosition());

        var baseRookScore = 5 * opennessModifier + twoRookModifier + positionalStrength;
        calculateBoardScore(rook.getPlayer(), baseRookScore);
    }

    protected void score(Queen queen) {
        var opennessModifier = 1.0;
//        int size = queen.possibleMoves(chessBoard).size();
//        if (size > 10) {
//            opennessModifier = 1.05;
//        } else if (size > 15) {
//            opennessModifier = 1.1;
//        } else if (size < 5) {
//            opennessModifier = .95;
//        }

        var positionalStrength = computePositionalStrengthScore(queen.getPosition());
        var baseKingScore = 10 * opennessModifier + positionalStrength;
        calculateBoardScore(queen.getPlayer(), baseKingScore);
    }

    protected void score(King king) {
        var opennessModifier = 1.0;
//        int size = king.possibleMoves(chessBoard).size();
//        if (size > 3) {
//            opennessModifier = 1.05;
//        } else if (size < 2) {
//            opennessModifier = .95;
//        }

        var positionalStrength = computePositionalStrengthScore(king.getPosition());
        var baseKingScore = 200 * opennessModifier + positionalStrength;
        calculateBoardScore(king.getPlayer(), baseKingScore);
    }

    protected void score(Bishop bishop) {
        var opennessModifier = 1.0;
//        int size = bishop.possibleMoves(chessBoard).size();
//        if (size > 6) {
//            opennessModifier = 1.1;
//        } else if (size < 4) {
//            opennessModifier = .9;
//        }

        var twoBishopModifier = 0.0;
        if (bishop.getPlayer().equals(player)) {
            if (++myBishops == 2) {
                twoBishopModifier = .5;
            }
        } else {
            if (++opponentsBishops == 2) {
                twoBishopModifier = .5;
            }
        }

        var positionalStrength = computePositionalStrengthScore(bishop.getPosition());

        var baseBishopScore = 3.5 * opennessModifier + twoBishopModifier + positionalStrength;
        calculateBoardScore(bishop.getPlayer(), baseBishopScore);
    }

    protected void score(Knight knight, ChessBoard chessBoard) {
        var opennessModifier = switch (knight.possibleMoves(chessBoard).size()) {
            case 0 -> .7;
            case 1 -> .8;
            case 2 -> 1.0;
            case 3 -> 1.2;
            case 4 -> 1.5;
            default -> 1;
        };

        var twoKnightModifier = 0.0;
        if (knight.getPlayer().equals(player)) {
            if (++myKnights == 2) {
                twoKnightModifier = .5;
            }
        } else {
            if (++opponentsKnights == 2) {
                twoKnightModifier = .5;
            }
        }

        var positionalStrength = computePositionalStrengthScore(knight.getPosition());

        var baseKnightScore = 3.25 * opennessModifier + twoKnightModifier + positionalStrength;
        calculateBoardScore(knight.getPlayer(), baseKnightScore);
    }

    protected void score(Pawn pawn) {
        double pawnOpennessModifier = 1;
//        var pawnOpennessModifier = switch (pawn.possibleMoves(chessBoard).size()) {
//            case 0 -> .95;
//            case 1 -> 1.0;
//            case 2 -> 1.05;
//            case 3 -> 1.1;
//            default -> 1;
//        };

        var rowLocationScore = switch (pawn.getPlayer()) {
            case black -> blackValues.get(pawn.getPosition().row());
            case white -> whiteValues.get(pawn.getPosition().row());
        };

        var positionalStrength = computePositionalStrengthScore(pawn.getPosition());

        var basePawnScore = 1.0 * pawnOpennessModifier + rowLocationScore + positionalStrength;
        calculateBoardScore(pawn.getPlayer(), basePawnScore);
    }

    public double getTotalScore() {
        return totalScore;
    }

    private void calculateBoardScore(Player piecePlayer, double pieceScore) {
        if (piecePlayer.equals(player)) {
            totalScore += pieceScore;
        } else {
            totalScore -= pieceScore;
        }
    }

    private float computePositionalStrengthScore(Position position) {
        if (position.column() == 3 || position.column() == 4) {
            return middleModifier;
        } else if (position.column() == 2 || position.column() == 5) {
            return Math.max(middleModifier - 1, 1);
        }
        return 0.0f;
    }

}
