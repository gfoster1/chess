package com.whitespace.board;

import com.whitespace.BestMoveService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.piece.*;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

public class DefaultChessBoard implements ChessBoard {
    public static final int MAX_BOARD_SIZE = 8;

    private static final Map<Class, Constructor> CLASS_CACHE = new HashMap<>();

    private final Stack<MoveResult> moveHistory = new Stack<>();
    private final List<Piece> activePieces = new ArrayList<>(32);
    private final List<Piece> capturedPieces = new ArrayList<>(32);

    private final BestMoveService blackBoardScoringService;
    private final BestMoveService whiteBoardScoringService;

    public DefaultChessBoard(BestMoveService blackBoardScoringService, BestMoveService whiteBoardScoringService) {
        this.blackBoardScoringService = blackBoardScoringService;
        this.whiteBoardScoringService = whiteBoardScoringService;
        initializeWhitePieces();
        initializeBlackPieces();
    }

    private void initializeBlackPieces() {
        for (int i = 0; i < 8; i++) {
            var position = new Position(6, i);
            var pawn = new Pawn(Player.black, position);
            activePieces.add(pawn);
        }

        Rook rook1 = new Rook(Player.black, new Position(7, 0));
        Knight knight1 = new Knight(Player.black, new Position(7, 1));
        Bishop bishop1 = new Bishop(Player.black, new Position(7, 2));
        Queen queen = new Queen(Player.black, new Position(7, 3));
        King king = new King(Player.black, new Position(7, 4));
        Bishop bishop2 = new Bishop(Player.black, new Position(7, 5));
        Knight knight2 = new Knight(Player.black, new Position(7, 6));
        Rook rook2 = new Rook(Player.black, new Position(7, 7));

        activePieces.add(rook1);
        activePieces.add(bishop1);
        activePieces.add(knight1);
        activePieces.add(queen);
        activePieces.add(king);
        activePieces.add(knight2);
        activePieces.add(bishop2);
        activePieces.add(rook2);
    }

    private void initializeWhitePieces() {
        for (int i = 0; i < 8; i++) {
            var position = new Position(1, i);
            var pawn = new Pawn(Player.white, position);
            activePieces.add(pawn);
        }

        Rook rook1 = new Rook(Player.white, new Position(0, 0));
        Knight knight1 = new Knight(Player.white, new Position(0, 1));
        Bishop bishop1 = new Bishop(Player.white, new Position(0, 2));
        Queen queen = new Queen(Player.white, new Position(0, 3));
        King king = new King(Player.white, new Position(0, 4));
        Bishop bishop2 = new Bishop(Player.white, new Position(0, 5));
        Knight knight2 = new Knight(Player.white, new Position(0, 6));
        Rook rook2 = new Rook(Player.white, new Position(0, 7));

        activePieces.add(rook1);
        activePieces.add(bishop1);
        activePieces.add(knight1);
        activePieces.add(queen);
        activePieces.add(king);
        activePieces.add(knight2);
        activePieces.add(bishop2);
        activePieces.add(rook2);
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
                System.out.println("White has no move - Black wins!");
                isPlaying.set(false);
            });
            System.out.println("White's move - end");

            if (isPlaying.get()) {
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
                    System.out.println("Black has no move - White wins!");
                    isPlaying.set(false);
                });
                System.out.println("Black's move - end");
            }
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
        moveResult.capturedPiece().ifPresent(piece -> System.out.printf("%s %s was captured%n", opposingPlayer, piece.getClass().getSimpleName()));

        if (moveResult.opponentWins()) {
            System.out.printf("%s beat %s with a resounding victory%n", opposingPlayer, currentPlayer);
        }

        if (moveResult.currentPlayerWins()) {
            System.out.printf("%s beat %s with a resounding victory%n", currentPlayer, opposingPlayer);
        }
    }

    private void printBoard() {
        Piece[][] pieces = new Piece[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
        activePieces.parallelStream()
                .forEach(piece -> {
                    var position = piece.getPosition();
                    pieces[position.row()][position.column()] = piece;
                });

        for (int i = 0; i < MAX_BOARD_SIZE; i++) {
            if (i == 0) {
                System.out.print("         ");
                for (int j = 0; j < MAX_BOARD_SIZE; j++) {
                    System.out.printf("%10s", "Col: " + j);
                }
                System.out.println();
            }
            System.out.print("Row: " + i + "   ");
            for (int j = 0; j < MAX_BOARD_SIZE; j++) {
                String format = "%10s";
                var piece = pieces[i][j];
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
        lastMove.capturedPiece()
                .ifPresent(capturedPiece -> {
                    capturedPieces.remove(capturedPiece);
                    activePieces.add(capturedPiece);
                    capturedPiece.setPosition(lastMove.destination());
                });
    }

    public MoveResult applyMove(Move move, boolean debug) {
        var pieceToBeMoved = move.piece();
        var origin = pieceToBeMoved.getPosition();
        var destination = move.destination();
        if (isInvalidMove(move)) {
            return new MoveResult(pieceToBeMoved, origin, destination, Optional.empty(), true, false);
        }

        var currentPlayerWins = new AtomicBoolean(false);

        var opponentWins = false;
        var player = pieceToBeMoved.getPlayer();
        var opposingPlayer = switch (player) {
            case white -> Player.black;
            case black -> Player.white;
        };

        // taking a piece
        Optional<Piece> possiblyCapturedPiece = activePieces.stream()
                .filter(piece -> piece.getPlayer().equals(opposingPlayer))
                .filter(piece -> piece.getPosition().column() == destination.column() &&
                        piece.getPosition().row() == destination.row())
                .findAny();

        possiblyCapturedPiece.ifPresent(piece -> {
            if (piece instanceof King) {
                currentPlayerWins.set(true);
            }

            activePieces.remove(piece);
            capturedPieces.add(piece);
        });

        pieceToBeMoved.setPosition(destination);

        // pawn to queen
        if (pieceToBeMoved instanceof Pawn) {
            if (isPawnReadyToBeQueened(player, destination)) {
                activePieces.remove(pieceToBeMoved);
                var queen = new Queen(player, destination);
                activePieces.add(queen);
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
                Optional.of(new Position(row, column)) :
                Optional.empty();
    }

    public boolean isSpaceTaken(Position position) {
        return activePieces.stream()
                .filter(piece -> piece.getPosition().row() == position.row() && piece.getPosition().column() == position.column())
                .limit(1)
                .count() >= 1;
    }

    public boolean isSpaceTakenByOpposingPlayerPiece(Position position, Player player) {
        return activePieces.stream()
                .limit(1)
                .filter(piece -> !piece.getPlayer().equals(player))
                .filter(piece -> piece.getPosition().row() == position.row() &&
                        piece.getPosition().column() == position.column())
                .count() >= 1;
    }

    public boolean isSpaceTakenByMyPiece(Position position, Player player) {
        return activePieces.stream()
                .filter(piece -> piece.getPlayer().equals(player))
                .filter(piece -> piece.getPosition().row() == position.row() &&
                        piece.getPosition().column() == position.column())
                .limit(1)
                .count() >= 1;
    }

    private boolean isInvalidMove(Move move) {
        return false;
//        return isKingMovingIntoCheck(move) || isKingInCheckAndDidNotMove(move);
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

    public List<Piece> getPieces() {
        return activePieces;
    }

    @Override
    public Stream<Move> getPossibleMoves(Player player) {
        return null;
//        return activePieces.parallelStream()
//                .filter(piece -> piece.getPlayer().equals(player))
//                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(this).parallelStream());
    }

    @Override
    public boolean isKingInCheck(Player player) {
        Optional<Position> playersKing = activePieces.stream().parallel()
                .filter(piece -> piece.getPlayer().equals(player))
                .filter(piece -> piece instanceof King).map(king -> king.getPosition())
                .findFirst();

        boolean isKingInCheck = false;
        if (playersKing.isPresent()) {
            var opposingPlayer = switch (player) {
                case white -> Player.black;
                case black -> Player.white;
            };
            isKingInCheck = this.getPossibleMoves(opposingPlayer)
                    .filter(move -> {
                        var destination = move.destination();
                        var playersKingPosition = playersKing.get();
                        return destination.row() == playersKingPosition.row() && destination.column() == playersKingPosition.column();
                    })
                    .limit(1)
                    .count() >= 1;
        } else {
            System.out.println("No king is present so returning false for now.");
            // TODO big potential bug here
        }
        return isKingInCheck;
    }

    @Override
    public ChessBoard copy() {
        DefaultChessBoard defaultChessBoard = new DefaultChessBoard(blackBoardScoringService, whiteBoardScoringService);
        defaultChessBoard.activePieces.clear();
        defaultChessBoard.capturedPieces.clear();
        this.activePieces.forEach(piece -> {
            try {
                Constructor<? extends Piece> constructor = CLASS_CACHE.compute(piece.getClass(), (clazz, constructor1) -> {
                    if (constructor1 == null) {
                        try {
                            return clazz.getConstructor(Player.class, Position.class);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    return constructor1;
                });
                Position position = new Position(piece.getPosition().row(), piece.getPosition().column());
                Player player = piece.getPlayer();
                Piece newPiece = constructor.newInstance(player, position);
                defaultChessBoard.activePieces.add(newPiece);
            } catch (Exception e) {
                System.err.println(e);
            }
        });

        this.capturedPieces.forEach(piece -> {
            try {
                Constructor<? extends Piece> constructor = CLASS_CACHE.compute(piece.getClass(), (clazz, constructor1) -> {
                    if (constructor1 == null) {
                        try {
                            return clazz.getConstructor(Player.class, Position.class);
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                    return constructor1;
                });
                Position position = new Position(piece.getPosition().row(), piece.getPosition().column());
                Player player = piece.getPlayer();
                Piece newPiece = constructor.newInstance(player, position);
                defaultChessBoard.capturedPieces.add(newPiece);
            } catch (Exception e) {
                System.err.println(e);
            }
        });
        return defaultChessBoard;
    }

    private boolean isKingMovingIntoCheck(Move move) {
        var currentPieceToMove = move.piece();
        var kingMovingIntoCheck = false;

        if (currentPieceToMove instanceof King king) {
            applyMove(move, false);
            ChessBoard chessBoard = this;
            kingMovingIntoCheck = activePieces.parallelStream()
                    .filter(piece -> !piece.getPlayer().equals(piece.getPlayer()))
                    .flatMap((Function<Piece, Stream<Move>>) opponentsPiece -> opponentsPiece.possibleMoves(chessBoard).parallelStream())
                    .filter(opponentMove -> opponentMove.destination().equals(move.destination()))
                    .count() >= 1;
            revertLastMove();
        }
        return kingMovingIntoCheck;
    }

    private boolean isKingInCheckAndDidNotMove(Move move) {
        var currentPlayer = move.piece().getPlayer();
        var optional = activePieces.parallelStream()
                .filter(piece -> (piece instanceof King) && piece.getPlayer().equals(currentPlayer))
                .map(piece -> piece.getPosition())
                .findFirst();
        var currentPlayerKing = optional.isPresent() ? optional.get() : null;

        if (currentPlayerKing == null) {
            return false;
        }

        ChessBoard chessBoard = this;
        var kingIsInCheck = activePieces.parallelStream()
                .filter(piece -> !piece.getPlayer().equals(piece.getPlayer()))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .filter(opponentMove -> opponentMove.destination().equals(currentPlayerKing))
                .count() >= 1;

        return kingIsInCheck && !(move.piece() instanceof King);
    }

}