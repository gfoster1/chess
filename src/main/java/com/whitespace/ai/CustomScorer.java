package com.whitespace.ai;

import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;
import com.whitespace.piece.*;

import java.util.*;

public class CustomScorer {
    private static final double[] BLACK_PAWN_ROW_VALUES = {10, 6, 5, 3, 1.4, 1.2, 1, 1};
    private static final double[] WHITE_PAWN_ROW_VALUES = {1, 1, 1.2, 1.4, 3, 5, 6, 10};

    private final List<Rook> myRooks = new ArrayList<>(2);
    private final List<Rook> opponentsRooks = new ArrayList<>(2);

    private final Player player;
    private final int middleModifier;

    private int myKnights = 0;
    private int myBishops = 0;
    private int opponentsBishops = 0;
    private int opponentsKnights = 0;

    public CustomScorer(Player player, int middleModifier) {
        this.player = player;
        this.middleModifier = middleModifier;
    }

    protected double score(Rook rook, List<Move> moves) {
        var opennessModifier = 1.0;
        int size = moves.size();
        if (size >= 4) {
            opennessModifier = 1.1;
        } else if (size < 2) {
            opennessModifier = .9;
        }

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
//        var positionalStrength = computePositionalStrengthScore(rook.getPosition());

        var baseRookScore = 5 * opennessModifier + twoRookModifier;
        return calculateBoardScore(rook.getPlayer(), baseRookScore);
    }

    protected double score(Queen queen, List<Move> moves) {
        var opennessModifier = 1.0;
        int size = moves.size();
        if (size >= 5 && size < 10) {
            opennessModifier = 1.05;
        } else if (size >= 10) {
            opennessModifier = 1.1;
        } else if (size < 5) {
            opennessModifier = .95;
        }

        var positionalStrength = computePositionalStrengthScore(queen.getPosition());
        var baseScore = (10 + positionalStrength) * opennessModifier;
        return calculateBoardScore(queen.getPlayer(), baseScore);
    }

    protected double score(King king, List<Move> moves) {
        var opennessModifier = 1.0;
        int size = moves.size();
        if (size > 3) {
            opennessModifier = 1.05;
        } else if (size < 2) {
            opennessModifier = .95;
        }

        var positionalStrength = computePositionalStrengthScore(king.getPosition());
        var baseKingScore = 200 * opennessModifier + positionalStrength;
        return calculateBoardScore(king.getPlayer(), baseKingScore);
    }

    protected double score(Bishop bishop, List<Move> moves) {
        var opennessModifier = 1.0;
        int size = moves.size();
        if (size >= 5) {
            opennessModifier = 1.1;
        } else if (size > 2) {
            opennessModifier = 1.05;
        } else if (size <= 2) {
            opennessModifier = .95;
        }

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
        var baseBishopScore = (3.5 + positionalStrength) * opennessModifier + twoBishopModifier;
        return calculateBoardScore(bishop.getPlayer(), baseBishopScore);
    }

    protected double score(Knight knight, List<Move> moves) {
        var opennessModifier = switch (moves.size()) {
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

        var baseKnightScore = (3.25 + positionalStrength) * opennessModifier + twoKnightModifier;
        return calculateBoardScore(knight.getPlayer(), baseKnightScore);
    }

    protected double score(Pawn pawn, List<Move> moves) {
        var pawnOpennessModifier = switch (moves.size()) {
            default -> 1;
        };

        var rowLocationScore = switch (pawn.getPlayer()) {
            case black -> BLACK_PAWN_ROW_VALUES[pawn.getPosition().row()];
            case white -> WHITE_PAWN_ROW_VALUES[pawn.getPosition().row()];
        };

        var positionalStrength = computePositionalStrengthScore(pawn.getPosition());

        var basePawnScore = (1.0 + rowLocationScore + positionalStrength) * pawnOpennessModifier;
        return calculateBoardScore(pawn.getPlayer(), basePawnScore);
    }

    private double calculateBoardScore(Player piecePlayer, double pieceScore) {
        return piecePlayer.equals(player) ? pieceScore : pieceScore * -1;

    }

    private int computePositionalStrengthScore(Position position) {
        // I want to push the pieces closer to the middle of the board
        if (position.column() == 3 || position.column() == 4) {
            return middleModifier;
        } else if (position.column() == 2 || position.column() == 5) {
            return Math.max(middleModifier - 1, 1);
        }
        return 0;
    }

}
