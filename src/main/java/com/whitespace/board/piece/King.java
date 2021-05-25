package com.whitespace.board.piece;

import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.Move;
import com.whitespace.board.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {
    public King(Player player, Position position) {
        super(player, position);
    }

    @Override
    public List<Move> possibleMoves(ChessBoard chessBoard) {
        List<Move> possibleMoves = new ArrayList<>(6);
        generatePossibleMove(chessBoard, possibleMoves, position.column(), position.row() + 1);
        generatePossibleMove(chessBoard, possibleMoves, position.column(), position.row() - 1);
        generatePossibleMove(chessBoard, possibleMoves, position.column() + 1, position.row());
        generatePossibleMove(chessBoard, possibleMoves, position.column() - 1, position.row());
        generatePossibleMove(chessBoard, possibleMoves, position.column() + 1, position.row() + 1);
        generatePossibleMove(chessBoard, possibleMoves, position.column() - 1, position.row() - 1);
        return possibleMoves;
    }

    private void generatePossibleMove(ChessBoard chessBoard, List<Move> possibleMoves, int column, int row) {
        if (column >= 0 && column <= 7 && row >= 0 && row <= 7) {
            var destination = new Position(row, column);
            if (!chessBoard.isSpaceTaken(destination) || chessBoard.isSpaceTakenByOpposingPlayerPiece(destination, player)) {
                var possibleMove = new Move(this, destination);
                possibleMoves.add(possibleMove);
            }
        }
    }
}
