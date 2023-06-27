package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.*;
import java.util.stream.Stream;

public abstract class Piece {
    protected Position position;
    protected final Player player;

    private boolean captured = false;

    protected Piece(Player player, Position position) {
        this.player = player;
        this.position = position;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Player getPlayer() {
        return player;
    }

    public Position getPosition() {
        return position;
    }

    public abstract List<Move> possibleMoves(ChessBoard board);

    public abstract Stream<Move> possibleStreamMoves(ChessBoard board);

    protected Stream<Move> generateMoves(boolean generateDiagonal, int maxDiagonal, boolean generateHorizontal, int maxHorizontal, ChessBoard chessBoard) {
        var row = position.row();
        var column = position.column();
        var maxBoardSize = 8;

        Stream.Builder<Move> builder = Stream.builder();
        for (int i = 1; i <= maxBoardSize; i++) {
            if (generateHorizontal && i < maxHorizontal + 1) {
                builder.add(new Move(this, new Position(row, column - i)));
                builder.add(new Move(this, new Position(row, column + i)));
                builder.add(new Move(this, new Position(row - i, column)));
                builder.add(new Move(this, new Position(row + i, column)));
            }

            if (generateDiagonal && i < maxDiagonal + 1) {
                builder.add(new Move(this, new Position(row + i, column + i)));
                builder.add(new Move(this, new Position(row + i, column - i)));
                builder.add(new Move(this, new Position(row - i, column + i)));
                builder.add(new Move(this, new Position(row - i, column - i)));
            }
        }

        final List<Piece> myPositions = new ArrayList<>(17);
        final List<Piece> opponentPositions = new ArrayList<>(17);
        switch (player) {
            case white -> {
                myPositions.addAll(chessBoard.getWhitePieces());
                opponentPositions.addAll(chessBoard.getBlackPieces());
            }
            case black -> {
                myPositions.addAll(chessBoard.getBlackPieces());
                opponentPositions.addAll(chessBoard.getWhitePieces());
            }
        }

        final var diagonalBlocks = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE};
        final var horizontalBlocks = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE};
        var debug = true;
        //System.out.println("position = " + position);
        //System.out.println("row = " + row);
        //System.out.println("column = " + column);
        Stream<Move> stream = builder.build()
                .filter(move -> {
                    Position destination = move.destination();
                    var c = destination.column();
                    var r = destination.row();
                    return r != row || c != column;
                })
                .filter(move -> {
                    var c = move.destination().column();
                    return c >= 0 && c < maxBoardSize;
                })
                .filter(move -> {
                    var r = move.destination().row();
                    return r >= 0 && r < maxBoardSize;
                })
                .filter(move -> {
                    // TODO This is a potential optimization since we can likely match at the end of process.  I need to think more about the math.
                    Optional<Position> potentialBlocker = myPositions.parallelStream()
                            .map(piece -> piece.getPosition())
                            .filter(position -> {
                                var dest = move.destination();
                                return position.column() == dest.column() && position.row() == dest.row();
                            })
                            .findAny();
                    //System.out.println("potential same blocker = " + potentialBlocker);

                    var openSpace = potentialBlocker.isEmpty();
                    if (potentialBlocker.isPresent()) {
                        Position destination = potentialBlocker.get();
                        var r = destination.row();
                        var c = destination.column();
                        checkIfBlockedHorizontally(row, column, horizontalBlocks, r, c, false);
                        checkIfBlockedDiagonally(row, column, diagonalBlocks, r, c, false);
                    }
                    return openSpace;
                })
                .peek(move -> {
                    // TODO This is a potential optimization since we can likely match at the end of process.  I need to think more about the math.
                    Optional<Position> potentialBlocker = opponentPositions.parallelStream()
                            .map(piece -> piece.getPosition())
                            .filter(position -> {
                                var dest = move.destination();
                                return position.column() == dest.column() && position.row() == dest.row();
                            })
                            .findAny();
                    //System.out.println("potential opponent blocker = " + potentialBlocker);
                    if (potentialBlocker.isPresent()) {
                        // the move is good but we need to block future moves past this point
                        var destination = potentialBlocker.get();
                        var r = destination.row();
                        var c = destination.column();
                        checkIfBlockedHorizontally(row, column, horizontalBlocks, r, c, true);
                        checkIfBlockedDiagonally(row, column, diagonalBlocks, r, c, true);
                    }
                })
                .peek(move -> {
                    if (debug) {
                        //System.out.println("filter (pre) moves = " + move);
                    }
                })
                .filter(move -> filterVerticalBlocks(row, column, horizontalBlocks, move))
                .filter(move -> filterDiagonalBlocks(row, column, diagonalBlocks, move))
                .peek(move -> {
                    if (debug) {
                        //System.out.println("filter (post) moves = " + move);
                        for (int block : horizontalBlocks) {
                            //System.out.println("block = " + block);
                        }
                    }
                });
        return stream;
    }

    private void checkIfBlockedDiagonally(int row, int column, int[] blockedArray, int r, int c, boolean inclusive) {
        int rise = c - column;
        int run = r - row;
        if (rise != 0 && run != 0 && Math.abs(rise / run) == 1) {
            //System.out.println("Blocker on a diagonal");
            //System.out.println("diagonal r = " + r);
            //System.out.println("diagonal c = " + c);
            if (r < row && c > column) {
                //System.out.println("Blocker on up");
                if (r > blockedArray[0]) {
                    blockedArray[0] = inclusive ? r - 1 : r;
                }
            }

            if (r > row && c > column) {
                //System.out.println("Blocker on left");
                if (r < blockedArray[1]) {
                    blockedArray[1] = inclusive ? r + 1 : r;
                }
            }

            if (r < row && c < column) {
                //System.out.println("Blocker on up");
                if (r > blockedArray[2]) {
                    blockedArray[2] = inclusive ? r - 1 : r;
                }
            }

            if (r > row && c < column) {
                //System.out.println("Blocker on the right");
                if (r < blockedArray[3]) {
                    blockedArray[3] = inclusive ? r + 1 : r;
                }
            }
        }
    }

    private static void checkIfBlockedHorizontally(int row, int column, int[] horizontalBlocks, int r, int c, boolean inclusive) {
        if (c == column) {
            if (r < row && r > horizontalBlocks[2]) {
                //System.out.println("Blocker on up");
                horizontalBlocks[2] = inclusive ? r - 1 : r;
            }

            if (r > row && r < horizontalBlocks[3]) {
                //System.out.println("Blocker on the up");
                horizontalBlocks[3] = inclusive ? r + 1 : r;
            }
        }

        if (r == row) {
            if (c < column && c > horizontalBlocks[0]) {
                //System.out.println("Blocker on left");
                horizontalBlocks[0] = inclusive ? c - 1 : c;
            }

            if (c > column && c < horizontalBlocks[1]) {
                //System.out.println("Blocker on the right");
                horizontalBlocks[1] = inclusive ? c + 1 : c;
            }
        }
    }

    private boolean filterDiagonalBlocks(int row, int column, int[] blockedArray, Move move) {
        var c = move.destination().column();
        var r = move.destination().row();
        int rise = c - column;
        int run = r - row;
        //System.out.println("run = " + run);
        //System.out.println("rise = " + rise);
        var notBlocked = true;
        if (rise != 0 && run != 0 && Math.abs(rise / run) == 1) {
            //System.out.println("Blocker on a diagonal");
            //System.out.println("diagonal r = " + r);
            //System.out.println("diagonal c = " + c);
            if (r < row && c > column) {
                //System.out.println("Blocker on up");
                notBlocked = r > blockedArray[0];
            }

            if (r > row && c > column) {
                //System.out.println("Blocker on left");
                notBlocked = r < blockedArray[1];
            }

            if (r < row && c < column) {
                //System.out.println("Blocker on up");
                notBlocked = r > blockedArray[2];
            }

            if (r > row && c < column) {
                //System.out.println("Blocker on the right");
                notBlocked = r < blockedArray[3];
            }
        }

        //System.out.println("notBlocked = " + notBlocked);
        return notBlocked;
    }

    private static boolean filterVerticalBlocks(int row, int column, int[] horizontalBlocks, Move move) {
        var notBlocked = true;
        var dest = move.destination();
        var c = dest.column();
        var r = dest.row();
        if (c == column) {
            //System.out.println("c = " + c);
            //System.out.println("horizontalBlocks = " + horizontalBlocks[0]);
            //System.out.println("move = " + move);
            if (r < row && horizontalBlocks[2] != Integer.MIN_VALUE) {
                notBlocked = r > horizontalBlocks[2];
                //System.out.println("not blocked to the left = " + notBlocked);
            }

            if (r > row && horizontalBlocks[3] != Integer.MAX_VALUE) {
                notBlocked = r < horizontalBlocks[3];
                //System.out.println("not blocked to the right = " + notBlocked);
            }
        }

        if (r == row) {
            //System.out.println("c = " + c);
            //System.out.println("horizontalBlocks = " + horizontalBlocks[0]);
            //System.out.println("move = " + move);
            if (c < column && horizontalBlocks[0] != Integer.MIN_VALUE) {
                notBlocked = c > horizontalBlocks[0];
                //System.out.println("not blocked to the left = " + notBlocked);
            }

            if (c > column && horizontalBlocks[1] != Integer.MAX_VALUE) {
                notBlocked = c < horizontalBlocks[1];
                //System.out.println("not blocked to the right = " + notBlocked);
            }
        }

        return notBlocked;
    }
}
