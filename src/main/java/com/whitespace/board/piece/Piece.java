package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
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

        var canGoUp = true;
        var canGoDown = true;
        var canGoLeft = true;
        var canGoRight = true;

        var canGoUpLeft = true;
        var canGoUpRight = true;
        var canGoDownLeft = true;
        var canGoDownRight = true;

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

        var opponentPositions = switch (player) {
            case white -> chessBoard.getWhitePieces();
            case black -> chessBoard.getBlackPieces();
        };

        System.out.println("position = " + position);
        System.out.println("row = " + row);
        System.out.println("column = " + column);
        return builder.build()
                .peek(move -> {
                    System.out.println("non filter moves = " + move);
                })
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
                    boolean pass = true;
                    if(generateHorizontal){
                        // put horizontal test logic here
                        pass = canGoRight;
                        pass = canGoLeft;
                    }
                    return pass;
                })
                .filter(move -> {
                    // TODO This is a potential optimization since we can likely match at the end of process.  I need to think more about the math.
                    boolean empty = opponentPositions.parallelStream()
                            .map(piece -> piece.getPosition())
                            .filter(position -> position.equals(move.destination()))
                            .findAny().isEmpty();
                    return empty;
                })
                .peek(move -> {
                    System.out.println("filter moves = " + move);
                })
                .parallel();
    }
}
