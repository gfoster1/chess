package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Piece {
    protected Position position;
    protected final Player player;

    protected Piece(Player player, Position position) {
        this.player = player;
        this.position = position;
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

        final List<Piece> myPositions = new ArrayList<>();
        final List<Piece> opponentPositions = new ArrayList<>();
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

        final var horizontalBlocks = new int[]{Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        var debug = true;
        System.out.println("position = " + position);
        System.out.println("row = " + row);
        System.out.println("column = " + column);
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
                            .filter(position -> position.equals(move.destination()))
                            .findAny();
                    System.out.println("potentialBlocker = " + potentialBlocker);

                    var openSpace = potentialBlocker.isEmpty();
                    if (potentialBlocker.isPresent()) {
                        Position destination = potentialBlocker.get();
                        var r = destination.row();
                        // CHECK if they are on the same row then see if there are column blockers
                        if (r == row) {
                            var c = destination.column();
                            if (c < column && c > horizontalBlocks[0]) {
                                System.out.println("Blocker on left");
                                horizontalBlocks[0] = c;
                            }

                            if (c > column && c < horizontalBlocks[1]) {
                                System.out.println("Blocker on the right");
                                horizontalBlocks[1] = c;
                            }
                        }
                    }
                    return openSpace;
                })
                .peek(move -> {
                    if (debug) {
                        System.out.println("filter (pre) moves = " + move);
                    }
                })
                .filter(move -> {
                    var notBlocked = true;
                    var dest = move.destination();
                    var r = dest.row();
                    if (r == row) {
                        var c = dest.column();
                        System.out.println("c = " + c);
                        System.out.println("horizontalBlocks = " + horizontalBlocks[0]);
                        System.out.println("move = " + move);
                        if (c < column && horizontalBlocks[0] != Integer.MIN_VALUE) {
                            notBlocked = c < horizontalBlocks[0];
                            System.out.println("not blocked to the left = " + notBlocked);
                        }

                        if (c > column && horizontalBlocks[1] != Integer.MAX_VALUE) {
                            System.out.println("not blocked to the right = " + notBlocked);
                            notBlocked = c > horizontalBlocks[1];
                        }
                    }
                    return notBlocked;
                })
                .peek(move -> {
                    if (debug) {
                        System.out.println("filter (post) moves = " + move);
                    }
                })
                .parallel();
        return stream;
    }
}
