package com.whitespace.ai.scoring;

import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;
import com.whitespace.board.piece.*;

import java.util.ArrayList;
import java.util.List;

public class StatefullMoveScorer {
    private static final double[] BLACK_PAWN_ROW_VALUES = {10, 6, 5, 2.2, 1.4, 1.2, 1, 1};
    private static final double[] WHITE_PAWN_ROW_VALUES = {1, 1, 1.2, 1.4, 2.2, 5, 6, 10};

    private final List<Rook> myRooks = new ArrayList<>(2);
    private final List<Rook> opponentsRooks = new ArrayList<>(2);

    private final Player player;
    private final int middleModifier;

    private boolean myKingTaken = true;
    private boolean opponentKingTaken = true;
    private int myKnights = 0;
    private int myBishops = 0;
    private int opponentsBishops = 0;
    private int opponentsKnights = 0;
    private double score = 0;

    public StatefullMoveScorer(Player player, int middleModifier) {
        this.player = player;
        this.middleModifier = middleModifier;
    }

    protected void score(Rook rook, List<Move> moves) {
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

        var baseRookScore = 5 * opennessModifier + twoRookModifier;
        calculateBoardScore(rook.getPlayer(), baseRookScore);
    }

    protected void score(Queen queen, List<Move> moves) {
        var opennessModifier = 1.0;
        int size = moves.size();
        if (size >= 10) {
            opennessModifier = 1.13;
        } else if (size >= 8) {
            opennessModifier = 1.1;
        } else if (size >= 4) {
            opennessModifier = 1.07;
        } else if (size <= 2) {
            opennessModifier = .98;
        }

        var positionalStrength = computeColumnPositionalStrengthScore(queen.getPosition());
        var baseScore = (10 + positionalStrength) * opennessModifier;
        calculateBoardScore(queen.getPlayer(), baseScore);
    }

    protected void score(King king, List<Move> moves) {
        if (king.getPlayer().equals(player)) {
            myKingTaken = false;
        } else {
            opponentKingTaken = false;
        }

        var opennessModifier = 1.0;
//        int size = moves.size();
//        if (size >= 5) {
//            opennessModifier = 1.02;
//        }

        var positionalStrength = computeColumnPositionalStrengthScore(king.getPosition());
        var baseKingScore = 200 * opennessModifier + positionalStrength;
        calculateBoardScore(king.getPlayer(), baseKingScore);
    }

    protected void score(Bishop bishop, List<Move> moves) {
        var opennessModifier = 1.0;
        int size = moves.size();
        if (size >= 5) {
            opennessModifier = 1.2;
        } else if (size > 2) {
            opennessModifier = 1.1;
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

        var positionalStrength = computeColumnPositionalStrengthScore(bishop.getPosition());
        var baseBishopScore = (3.5 + positionalStrength) * opennessModifier + twoBishopModifier;
        calculateBoardScore(bishop.getPlayer(), baseBishopScore);
    }

    protected void score(Knight knight, List<Move> moves) {
        var opennessModifier = switch (moves.size()) {
            case 0 -> .7;
            case 1 -> .8;
            case 2 -> 1.1;
            case 3 -> 1.3;
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

        var positionalStrength = computeColumnPositionalStrengthScore(knight.getPosition());

        var baseKnightScore = (3.25 + positionalStrength) * opennessModifier + twoKnightModifier;
        calculateBoardScore(knight.getPlayer(), baseKnightScore);
    }

    protected void score(Pawn pawn, List<Move> moves) {
        var pawnOpennessModifier = switch (moves.size()) {
            default -> 1;
        };

        var rowLocationScore = switch (pawn.getPlayer()) {
            case black -> BLACK_PAWN_ROW_VALUES[pawn.getPosition().row()];
            case white -> WHITE_PAWN_ROW_VALUES[pawn.getPosition().row()];
        };

        var positionalStrength = computeColumnPositionalStrengthScore(pawn.getPosition());

        var basePawnScore = (1.0 + rowLocationScore + positionalStrength) * pawnOpennessModifier;
        calculateBoardScore(pawn.getPlayer(), basePawnScore);
    }

    private double calculateBoardScore(Player piecePlayer, double pieceScore) {
        var calculatePieceScore = piecePlayer.equals(player) ? pieceScore : pieceScore * -1;
        return score = score + calculatePieceScore;
    }

    private int computeColumnPositionalStrengthScore(Position position) {
        // I want to push the pieces closer to the middle of the board
        if (position.column() == 3 || position.column() == 4) {
            return middleModifier;
        } else if (position.column() == 2 || position.column() == 5) {
            return Math.max(middleModifier - 1, 0);
        }
        return 0;
    }

    public double calculateFinalScore() {
        // is king taken from the board
        if (myKingTaken) {
            return -200;
        }

        if (opponentKingTaken) {
            return 200;
        }

        if (myRooks.size() == 2) {

        }

        if (opponentsRooks.size() == 2) {

        }

        // is king in check and can't move
        return score;
    }
}
