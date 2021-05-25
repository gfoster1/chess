package com.whitespace.board;

import com.whitespace.BestMoveService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.piece.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public DefaultChessBoard(Piece[][] pieces, BestMoveService blackBoardScoringService, BestMoveService whiteBoardScoringService) {
        this.blackBoardScoringService = blackBoardScoringService;
        this.whiteBoardScoringService = whiteBoardScoringService;
        initializePosition();
        for (int i = 0; i < MAX_BOARD_SIZE; i++) {
            for (int j = 0; j < MAX_BOARD_SIZE; j++) {
                this.pieces[i][j] = pieces[i][j];
                Piece piece = pieces[i][j];
                if (piece != null) {
                    pieceList.add(piece);
                }
            }
        }
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
            var whitesMove = whiteBoardScoringService.findBestMove(new DefaultChessBoard(this.pieces, blackBoardScoringService, whiteBoardScoringService));
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
                var blacksMove = blackBoardScoringService.findBestMove(new DefaultChessBoard(this.pieces, blackBoardScoringService, whiteBoardScoringService));
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
            if (i == 0) {
                System.out.print("         ");
                for (int j = 0; j < MAX_BOARD_SIZE; j++) {
                    System.out.print(String.format("%10s", "Col: " + j));
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
        var pieceToBeMoved = move.piece();
        var origin = pieceToBeMoved.getPosition();
        var destination = move.destination();
        if (isInvalidMove(move)) {
            return new MoveResult(pieceToBeMoved, origin, destination, Optional.empty(), true, false);
        }

        var currentPlayerWins = new AtomicBoolean(false);

        var opponentWins = false;
        var player = pieceToBeMoved.getPlayer();

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

    private boolean isInvalidMove(Move move) {
        return isKingMovingIntoCheck(move) || isKingInCheckAndDidNotMove(move);
    }

    private boolean isKingMovingIntoCheck(Move move) {
        var currentPieceToMove = move.piece();
        var kingMovingIntoCheck = false;

        if (currentPieceToMove instanceof King king) {
            applyMove(move, false);
            ChessBoard chessBoard = this;
            kingMovingIntoCheck = pieceList.parallelStream()
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
        var optional = pieceList.parallelStream()
                .filter(piece -> (piece instanceof King) && piece.getPlayer().equals(currentPlayer))
                .map(piece -> piece.getPosition())
                .findFirst();
        var currentPlayerKing = optional.isPresent() ? optional.get() : null;

        if (currentPlayerKing == null) {
            return false;
        }

        ChessBoard chessBoard = this;
        var kingIsInCheck = pieceList.parallelStream()
                .filter(piece -> !piece.getPlayer().equals(piece.getPlayer()))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(chessBoard).parallelStream())
                .filter(opponentMove -> opponentMove.destination().equals(currentPlayerKing))
                .count() >= 1;

        return kingIsInCheck && !(move.piece() instanceof King);
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
        pieces[7][1] = knight1;
        pieces[7][2] = bishop1;
        pieces[7][3] = queen;
        pieces[7][4] = king;
        pieces[7][5] = bishop2;
        pieces[7][6] = knight2;
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
        pieces[0][1] = knight1;
        pieces[0][2] = bishop1;
        pieces[0][3] = queen;
        pieces[0][4] = king;
        pieces[0][5] = bishop2;
        pieces[0][6] = knight2;
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

    @Override
    public Stream<Move> getPossibleMoves(Player player) {
        return pieceList.parallelStream()
                .filter(piece -> piece.getPlayer().equals(player))
                .flatMap((Function<Piece, Stream<Move>>) piece -> piece.possibleMoves(this).parallelStream());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof DefaultChessBoard)) return false;

        DefaultChessBoard that = (DefaultChessBoard) o;

        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(pieceList, that.pieceList);
        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 37);
        Collections.sort(pieceList, Comparator.comparing(Piece::getPlayer));
        for (int i = 0; i < pieceList.size(); i++) {
            Piece piece = pieceList.get(i);
            int row = piece.getPosition().row();
            int column = piece.getPosition().column();
            String player = piece.getPlayer().toString();
            String simpleName = piece.getClass().getSimpleName();
            hashCodeBuilder.append(row);
            hashCodeBuilder.append(column);
            hashCodeBuilder.append(player);
            hashCodeBuilder.append(simpleName);
        }
        return hashCodeBuilder.toHashCode();
    }
}
