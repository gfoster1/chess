package com.whitespace.piece;

import com.whitespace.Board;
import com.whitespace.Player;
import com.whitespace.movement.Move;
import com.whitespace.movement.Position;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public abstract List<Move> possibleMoves(Board board);

    protected Stream.Builder<Move> generateValidHorizontalMoves(Stream.Builder<Move> builder,
                                                                Board board) {
        var upAvailable = new AtomicBoolean(true);
        var downAvailable = new AtomicBoolean(true);
        var rightAvailable = new AtomicBoolean(true);
        var leftAvailable = new AtomicBoolean(true);
        var maxBoardSize = 8;
        for (int i = 0; i < maxBoardSize; i++) {
            var row = position.row() - i;
            var column = position.column();
            doStuff(builder, leftAvailable, row, column, board, this);

            row = position.row() + i;
            column = position.column();
            doStuff(builder, rightAvailable, row, column, board, this);

            row = position.row();
            column = position.column() - i;
            doStuff(builder, downAvailable, row, column, board, this);

            row = position.row();
            column = position.column() + i;
            doStuff(builder, upAvailable, row, column, board, this);
        }
        return builder;
    }

    private void doStuff(Stream.Builder<Move> builder,
                         AtomicBoolean availableDirection,
                         int row, int column,
                         Board board,
                         Piece piece) {
        if (row == position.row() && column == position.column()) {
            return;
        }

        board.getPosition(row, column).ifPresentOrElse(
                destination -> {
                    if (availableDirection.get()) {
                        if (board.isSpaceTakenByMyPiece(destination, player)) {
                            availableDirection.set(false);
                            return;
                        }

                        if (board.isSpaceTakenByOpposingPlayerPiece(destination, player)) {
                            availableDirection.set(false);
                        }
                        builder.add(new Move(piece, destination));
                    }
                },
                () -> availableDirection.set(false));
    }

    protected Stream.Builder<Move> generateValidDiagonalMoves(Stream.Builder<Move> builder,
                                                              Board board) {
        var upAvailable = new AtomicBoolean(true);
        var downAvailable = new AtomicBoolean(true);
        var rightAvailable = new AtomicBoolean(true);
        var leftAvailable = new AtomicBoolean(true);
        var maxBoardSize = 8;
        for (int i = 0; i < maxBoardSize; i++) {
            var row = position.row() - i;
            var column = position.column() - i;
            doStuff(builder, leftAvailable, row, column, board, this);

            row = position.row() + i;
            column = position.column() + i;
            doStuff(builder, rightAvailable, row, column, board, this);

            row = position.row() + i;
            column = position.column() - i;
            doStuff(builder, downAvailable, row, column, board, this);

            row = position.row() - i;
            column = position.column() + i;
            doStuff(builder, upAvailable, row, column, board, this);
        }
        return builder;
    }
}
