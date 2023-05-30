package com.whitespace.ai.scoring;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.piece.*;

import java.util.HashMap;
import java.util.Map;

public class DefaultBoardScoringService implements BoardScoreService {
    private final Player player;
    private final int middleModifier;

    public DefaultBoardScoringService(Player player, int middleModifier) {
        this.middleModifier = middleModifier;
        this.player = player;
    }

    @Override
    public double scoreBoard(ChessBoard chessBoard) {

        var scorer = new Scorer(player, middleModifier);
        chessBoard.getPieces().parallelStream()
                .forEach(piece -> {
                            scorer.computePositionalStrengthScore(piece);
                            if (piece instanceof Rook rook) {
                                scorer.score(rook);
                            }

                            if (piece instanceof Bishop bishop) {
                                scorer.score(bishop);
                            }

                            if (piece instanceof Knight knight) {
                                scorer.score(knight);
                            }

                            if (piece instanceof Queen rook) {
                                scorer.score(rook);
                            }

                            if (piece instanceof King king) {
                                scorer.score(king);
                            }

                            if (piece instanceof Pawn pawn) {
                                scorer.score(pawn);
                            }
                        }
                );

        var score = scorer.getTotalScore();
        return score;
    }

    private final static class Scorer {
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

        private int myRooks = 0;
        private int opponentsRooks = 0;
        private int myBishops = 0;
        private int opponentsBishops = 0;
        private int myKnights = 0;
        private int opponentsKnights = 0;

        private int rookScore = 0;
        private int queenScore = 0;
        private int pawnScore = 0;
        private int knightScore = 0;
        private int bishopScore = 0;
        private int kingScore = 0;
        private int positionalStrengthScore = 0;

        private Scorer(Player player, int middleModifier) {
            this.player = player;
            this.middleModifier = middleModifier;
        }

        protected void computePositionalStrengthScore(Piece piece) {
            if (piece.getPosition().column() == 3 || piece.getPosition().column() == 4) {
                if (piece.getPlayer().equals(player)) {
                    positionalStrengthScore += middleModifier;
                } else {
                    positionalStrengthScore -= middleModifier;
                }
            }
        }

        protected void score(Rook rook) {
            if (rook.getPlayer().equals(player)) {
                myRooks++;
            } else {
                opponentsRooks++;
            }
        }

        protected void score(Queen queen) {
            if (queen.getPlayer().equals(player)) {
                queenScore += 10;
            } else {
                queenScore -= 10;
            }
        }

        protected void score(King king) {
            if (king.getPlayer().equals(player)) {
                kingScore += 200;
            } else {
                kingScore -= 200;
            }
        }

        protected void score(Bishop bishop) {
            if (bishop.getPlayer().equals(player)) {
                myBishops++;
            } else {
                opponentsBishops++;
            }
        }

        protected void score(Knight knight) {
            if (knight.getPlayer().equals(player)) {
                myKnights++;
            } else {
                opponentsKnights++;
            }
        }

        protected void score(Pawn pawn) {
            int score;
            if (pawn.getPlayer().equals(Player.black)) {
                score = blackValues.get(pawn.getPosition().row());
            } else {
                score = whiteValues.get(pawn.getPosition().row());
            }

            if (pawn.getPlayer().equals(player)) {
                pawnScore += score;
            } else {
                pawnScore -= score;
            }
        }

        protected int getTotalScore() {
            computeKnightScore();
            computeBishopScore();
            computeRookScore();
            return kingScore + queenScore + rookScore + bishopScore + knightScore + pawnScore + positionalStrengthScore;
        }

        private void computeKnightScore() {
            var oneKnight = 3;
            var twoKnights = 5;
            if (myKnights == 2) {
                knightScore += twoKnights;
            } else if (myKnights == 1) {
                knightScore += oneKnight;
            }

            if (opponentsKnights == 2) {
                knightScore -= twoKnights;
            } else if (opponentsKnights == 1) {
                knightScore -= oneKnight;
            }
        }

        private void computeBishopScore() {
            var oneBishop = 3;
            var twoBishops = 5;
            if (myBishops == 2) {
                bishopScore += twoBishops;
            } else if (myBishops == 1) {
                bishopScore += oneBishop;
            }

            if (opponentsBishops == 2) {
                bishopScore -= twoBishops;
            } else if (opponentsBishops == 1) {
                bishopScore -= oneBishop;
            }
        }

        private void computeRookScore() {
            var oneRook = 5;
            var twoRooks = 7;

            if (myRooks == 2) {
                rookScore += twoRooks;
            } else if (myRooks == 1) {
                rookScore += oneRook;
            }

            if (opponentsRooks == 2) {
                rookScore -= twoRooks;
            } else if (opponentsRooks == 1) {
                rookScore -= oneRook;
            }
        }

    }
}
