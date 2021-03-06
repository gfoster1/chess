package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

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

    public abstract List<Move> possibleMoves(ChessBoard chessBoard);

    protected Stream.Builder<Move> generateValidHorizontalMoves(Stream.Builder<Move> builder,
                                                                ChessBoard board,
                                                                int size) {
        var upAvailable = new AtomicBoolean(true);
        var downAvailable = new AtomicBoolean(true);
        var rightAvailable = new AtomicBoolean(true);
        var leftAvailable = new AtomicBoolean(true);
        for (int i = 0; i < size; i++) {
            var row = position.row() - i;
            var column = position.column();
            generateMoves(builder, leftAvailable, row, column, board, this);

            row = position.row() + i;
            column = position.column();
            generateMoves(builder, rightAvailable, row, column, board, this);

            row = position.row();
            column = position.column() - i;
            generateMoves(builder, downAvailable, row, column, board, this);

            row = position.row();
            column = position.column() + i;
            generateMoves(builder, upAvailable, row, column, board, this);
        }
        return builder;
    }

    protected Stream.Builder<Move> generateValidDiagonalMoves(Stream.Builder<Move> builder,
                                                              ChessBoard board,
                                                              int size) {
        var upAvailable = new AtomicBoolean(true);
        var downAvailable = new AtomicBoolean(true);
        var rightAvailable = new AtomicBoolean(true);
        var leftAvailable = new AtomicBoolean(true);
        for (int i = 0; i < size; i++) {
            var row = position.row() - i;
            var column = position.column() - i;
            generateMoves(builder, leftAvailable, row, column, board, this);

            row = position.row() + i;
            column = position.column() + i;
            generateMoves(builder, rightAvailable, row, column, board, this);

            row = position.row() + i;
            column = position.column() - i;
            generateMoves(builder, downAvailable, row, column, board, this);

            row = position.row() - i;
            column = position.column() + i;
            generateMoves(builder, upAvailable, row, column, board, this);
        }
        return builder;
    }

    private void generateMoves(Stream.Builder<Move> builder,
                               AtomicBoolean availableDirection,
                               int row, int column,
                               ChessBoard board,
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
}
