package com.whitespace;

import com.whitespace.movement.Move;
import com.whitespace.movement.MoveResult;
import com.whitespace.movement.Position;
import com.whitespace.piece.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultChessBoard implements ChessBoard {
    public static final int MAX_BOARD_SIZE = 8;

    private final BestMoveService blackBoardScoringService;
    private final BestMoveService whiteBoardScoringService;
    private final Stack<MoveResult> moveHistory = new Stack<>();

    private final Piece[][] pieces = new Piece[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
    private final Position[][] positions = new Position[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
    private final List<Piece> pieceList = new ArrayList<>(32);

    public DefaultChessBoard(BestMoveService blackBoardScoringService, BestMoveService whiteBoardScoringService) {
        this.blackBoardScoringService = blackBoardScoringService;
        this.whiteBoardScoringService = whiteBoardScoringService;
        initializePosition();
        initializeWhitePieces();
        initializeBlackPieces();
    }

    private void initializePosition() {
        for (int i = 0; i < MAX_BOARD_SIZE; i++) {
            for (int j = 0; j < MAX_BOARD_SIZE; j++) {
                positions[i][j] = new Position(i, j);
            }
        }
    }

    public void play() {
        final AtomicBoolean isPlaying = new AtomicBoolean(true);
        while (isPlaying.get()) {
            System.out.println("White's move - begin");
            var whitesMove = whiteBoardScoringService.findBestMove(this);
            whitesMove.ifPresentOrElse(move -> {
                MoveResult moveResult = applyMove(move, true);

                if (moveResult.opponentWins()) {
                    isPlaying.set(false);
                } else if (moveResult.currentPlayerWins()) {
                    isPlaying.set(false);
                }

            }, () -> {
                System.out.println("Black wins!");
                isPlaying.set(false);
            });
            System.out.println("White's move - end");

            System.out.println("Black's move - begin");
            var blacksMove = blackBoardScoringService.findBestMove(this);
            blacksMove.ifPresentOrElse(move -> {
                MoveResult moveResult = applyMove(move, true);

                if (moveResult.opponentWins()) {
                    isPlaying.set(false);
                } else if (moveResult.currentPlayerWins()) {
                    isPlaying.set(false);
                }

            }, () -> {
                System.out.println("White wins!");
                isPlaying.set(false);
            });
            System.out.println("Black's move - end");
        }
    }

    private void printMoveResult(MoveResult moveResult) {
        printBoard();
        var currentPlayer = moveResult.piece().getPlayer();
        var output = String.format("%s moved %s from %s to %s", currentPlayer,
                moveResult.piece().getClass().getSimpleName(),
                moveResult.origin(), moveResult.destination());
        System.out.println(output);
        var opposingPlayer = switch (currentPlayer) {
            case white -> Player.black;
            case black -> Player.white;
        };
        moveResult.capturedPiece().ifPresent(piece -> {
            System.out.println(String.format("%s %s was captured", opposingPlayer, piece.getClass().getSimpleName()));
        });

        if (moveResult.opponentWins()) {
            System.out.println(String.format("%s beat %s with a resounding victory", opposingPlayer, currentPlayer));
        }

        if (moveResult.currentPlayerWins()) {
            System.out.println(String.format("%s beat %s with a resounding victory", currentPlayer, opposingPlayer));
        }
    }

    private void printBoard() {
        for (int i = 0; i < MAX_BOARD_SIZE; i++) {
            System.out.println("Row: " + i);
            for (int j = 0; j < MAX_BOARD_SIZE; j++) {
                var piece = pieces[i][j];
                String format = "%10s";
                if (piece == null) {
                    System.out.print(String.format(format, "x"));
                } else {
                    var player = switch (piece.getPlayer()) {
                        case black -> 'b';
                        case white -> 'w';
                    };
                    System.out.print(String.format(format, player + piece.getClass().getSimpleName()));
                }
            }
            System.out.println();
        }
    }

    public void revertLastMove() {
        var lastMove = moveHistory.pop();
        var origin = lastMove.origin();
        lastMove.piece().setPosition(origin);
        pieces[origin.row()][origin.column()] = lastMove.piece();
        lastMove.capturedPiece()
                .ifPresentOrElse(capturedPiece -> {
                    var destination = lastMove.destination();
                    capturedPiece.setPosition(destination);
                    pieces[destination.row()][destination.column()] = capturedPiece;
                    pieceList.add(capturedPiece);
                }, () -> {
                    var destination = lastMove.destination();
                    pieces[destination.row()][destination.column()] = null;
                });
    }

    public MoveResult applyMove(Move move, boolean debug) {
        var currentPlayerWins = new AtomicBoolean(false);
        if (isInvalidMove(move)) {
            return new MoveResult(null, null, null, null, false, false);
        }

        var opponentWins = false;
        var pieceToBeMoved = move.piece();
        var destination = move.destination();
        var player = pieceToBeMoved.getPlayer();
        var origin = pieceToBeMoved.getPosition();

        // taking a piece
        var capturedPiece = pieces[destination.row()][destination.column()];
        Optional<Piece> possiblyCapturedPiece = Optional.ofNullable(capturedPiece);
        if (capturedPiece != null) {
            if (capturedPiece.getPlayer().equals(player)) {
                if (debug) {
                    // invalid move that likely won't occur
                    System.out.println("invalid move!");
                }
                opponentWins = true;
            } else {
                pieceList.remove(capturedPiece);
            }

            if (capturedPiece instanceof King) {
                currentPlayerWins.set(true);
            }
        }

        pieceToBeMoved.setPosition(destination);
        pieces[destination.row()][destination.column()] = pieceToBeMoved;
        pieces[origin.row()][origin.column()] = null;

        // pawn to queen
        if (pieceToBeMoved instanceof Pawn) {
            if (isPawnReadyToBeQueened(player, destination)) {
                var queen = new Queen(player, destination);
                pieces[destination.row()][destination.column()] = queen;
                pieces[origin.row()][origin.column()] = null;
            }

            if (isPawnDoingEnpassant(player, destination)) {
                // todo handle this
            }
        }
        var moveResult = new MoveResult(pieceToBeMoved, origin, destination, possiblyCapturedPiece, opponentWins,
                currentPlayerWins.get());
        if (debug) {
            printMoveResult(moveResult);
        }
        return moveHistory.push(moveResult);
    }

    public Optional<Position> getPosition(int row, int column) {
        return (row >= 0 && row < MAX_BOARD_SIZE && column >= 0 && column < MAX_BOARD_SIZE) ?
                Optional.of(positions[row][column]) :
                Optional.empty();
    }

    public boolean isSpaceTaken(Position position) {
        return pieces[position.row()][position.column()] != null;
    }

    public boolean isSpaceTakenByOpposingPlayerPiece(Position destination, Player player) {
        var piece = pieces[destination.row()][destination.column()];
        if (piece == null) {
            return false;
        }
        return !piece.getPlayer().equals(player);
    }

    public boolean isSpaceTakenByMyPiece(Position destination, Player player) {
        var p = pieces[destination.row()][destination.column()];
        if (p == null) {
            return false;
        }
        return p.getPlayer().equals(player);
    }

    public boolean isInvalidMove(Move move) {
        return isKingMovingIntoCheck(move);
    }

    private boolean isKingMovingIntoCheck(Move move) {
        return false;
    }

    private boolean isPawnDoingEnpassant(Player player, Position destination) {
        return false;
    }

    private boolean isPawnReadyToBeQueened(Player player, Position destination) {
        int whiteBackRow = 7;
        int blackBackRow = 0;
        return (player.equals(Player.white) && destination.row() == whiteBackRow) ||
                (player.equals(Player.black) && destination.row() == blackBackRow);
    }

    private void initializeBlackPieces() {
        for (int i = 0; i < 8; i++) {
            var pawn = new Pawn(Player.black, positions[6][i]);
            pieces[6][i] = pawn;
            pieceList.add(pawn);
        }

        Rook rook1 = new Rook(Player.black, positions[7][0]);
        Knight knight1 = new Knight(Player.black, positions[7][1]);
        Bishop bishop1 = new Bishop(Player.black, positions[7][2]);
        Queen queen = new Queen(Player.black, positions[7][3]);
        King king = new King(Player.black, positions[7][4]);
        Bishop bishop2 = new Bishop(Player.black, positions[7][5]);
        Knight knight2 = new Knight(Player.black, positions[7][6]);
        Rook rook2 = new Rook(Player.black, positions[7][7]);

        pieces[7][0] = rook1;
        pieces[7][1] = bishop1;
        pieces[7][2] = knight1;
        pieces[7][3] = queen;
        pieces[7][4] = king;
        pieces[7][5] = knight2;
        pieces[7][6] = bishop2;
        pieces[7][7] = rook2;

        pieceList.add(rook1);
        pieceList.add(bishop1);
        pieceList.add(knight1);
        pieceList.add(queen);
        pieceList.add(king);
        pieceList.add(knight2);
        pieceList.add(bishop2);
        pieceList.add(rook2);
    }

    private void initializeWhitePieces() {
        for (int i = 0; i < 8; i++) {
            var pawn = new Pawn(Player.white, positions[1][i]);
            pieces[1][i] = pawn;
            pieceList.add(pawn);
        }

        Rook rook1 = new Rook(Player.white, positions[0][0]);
        Knight knight1 = new Knight(Player.white, positions[0][1]);
        Bishop bishop1 = new Bishop(Player.white, positions[0][2]);
        Queen queen = new Queen(Player.white, positions[0][3]);
        King king = new King(Player.white, positions[0][4]);
        Bishop bishop2 = new Bishop(Player.white, positions[0][5]);
        Knight knight2 = new Knight(Player.white, positions[0][6]);
        Rook rook2 = new Rook(Player.white, positions[0][7]);

        pieces[0][0] = rook1;
        pieces[0][1] = bishop1;
        pieces[0][2] = knight1;
        pieces[0][3] = queen;
        pieces[0][4] = king;
        pieces[0][5] = knight2;
        pieces[0][6] = bishop2;
        pieces[0][7] = rook2;

        pieceList.add(rook1);
        pieceList.add(bishop1);
        pieceList.add(knight1);
        pieceList.add(queen);
        pieceList.add(king);
        pieceList.add(knight2);
        pieceList.add(bishop2);
        pieceList.add(rook2);
    }

    public List<Piece> getPieces() {
        return pieceList;
    }

}
