package com.whitespace.board;

import com.whitespace.BestMoveService;
import com.whitespace.ChessBoard;
import com.whitespace.Player;
import com.whitespace.board.piece.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultChessBoard implements ChessBoard {
    private final Stack<String> moves = new Stack<>();
    private final List<Piece> blackPieces = new ArrayList<>(16);
    private final List<Piece> whitePieces = new ArrayList<>(16);
    private final BestMoveService blackBoardScoringService;
    private final BestMoveService whiteBoardScoringService;

    public DefaultChessBoard(BestMoveService blackBoardScoringService, BestMoveService whiteBoardScoringService) {
        this(blackBoardScoringService, whiteBoardScoringService, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public DefaultChessBoard(BestMoveService blackBoardScoringService, BestMoveService whiteBoardScoringService, String fen) {
        this.blackBoardScoringService = blackBoardScoringService;
        this.whiteBoardScoringService = whiteBoardScoringService;
        loadFromFEN(fen);
        moves.push(fen);
    }

    @Override
    public void play() {

    }

    @Override
    public Optional<String> applyMove(Move move, boolean debug) {
        List<Piece> myPieces = switch (move.piece().getPlayer()) {
            case white -> whitePieces;
            case black -> blackPieces;
        };

        var piece = move.piece();
        int index = myPieces.indexOf(piece);
        if (index < 0) {
            // piece isn't found
            return Optional.empty();
        }

        List<Piece> opponentPieces = switch (move.piece().getPlayer()) {
            case white -> blackPieces;
            case black -> whitePieces;
        };
        var destination = move.destination();
        Optional<Piece> somethingToTake = opponentPieces.parallelStream()
                .filter(p -> {
                    var opponentPosition = p.getPosition();
                    return opponentPosition.row() == destination.row() && opponentPosition.column() == destination.column();
                })
                .findAny();
        if (somethingToTake.isPresent()) {
            var opponentPiece = somethingToTake.get();
            var msg = String.format("%s %s at %s %s has taken opponent %s %s at %s,%s",
                    piece.getPlayer(),
                    piece.getClass().getSimpleName(),
                    piece.getPosition().row(),
                    piece.getPosition().column(),
                    opponentPiece.getPlayer(),
                    opponentPiece.getClass().getSimpleName(),
                    opponentPiece.getPosition().row(),
                    opponentPiece.getPosition().column());
            System.out.println(msg);
            opponentPieces.remove(opponentPiece);
        }

        myPieces.get(index).setPosition(move.destination());
        if (isPawnPromoted(piece)) {
            Queen queen = new Queen(piece.getPlayer(), move.destination());
            myPieces.remove(index);
            myPieces.add(queen);
        }

        var newFen = translateToFEN();
        moves.push(newFen);
        return Optional.of(newFen);
    }

    private boolean isPawnPromoted(Piece piece) {
        boolean isPawn = piece.getClass().equals(Pawn.class);
        // rough check here
        boolean isBackRow = piece.getPosition().row() == 0 || piece.getPosition().row() == 7;
        return isPawn && isBackRow;
    }

    @Override
    public List<Piece> getBlackPieces() {
        return blackPieces;
    }

    @Override
    public List<Piece> getWhitePieces() {
        return whitePieces;
    }

    @Override
    public Optional<String> rollbackToPreviousMove() {
        String lastMoveFEN = null;
        int size = moves.size();
        if (size >= 2) {
            moves.pop();
            lastMoveFEN = moves.peek();
            loadFromFEN(lastMoveFEN);
        } else if (size == 1) {
            lastMoveFEN = moves.peek();
        }
        return Optional.ofNullable(lastMoveFEN);
    }

    public void loadFromFEN(String fen) {
        moves.clear();
        blackPieces.clear();
        whitePieces.clear();
        String[] piecePlacement = fen.split("/");
        // will split the piece placement into entries, but last entry has the remaining
        // of the fen string so it needs to be dropped off.
        piecePlacement[7] = piecePlacement[7].split(" ")[0]; // drop the remaining of fen

        String[] fenInfo = fen.split(" "); // will split the last, just ignore the first entry as its piece placement.
        String sideToMove = fenInfo[1];
        String castling = fenInfo[2];
        String enPassant = fenInfo[3];
        String halfMoveClock = fenInfo[4];
        String fullMoveCounter = fenInfo[5];

        for (int i = 0; i < 8; i++) {
            int offset = 0;
            for (int j = 0; j < piecePlacement[i].length(); j++) {
                var c = piecePlacement[i].charAt(j);
                if (Character.isDigit(c)) {
                    // subtract 1 because fen is 1 based
                    offset = Character.getNumericValue(c) + offset - 1;
                } else if (Character.isAlphabetic(c)) {
                    var offsetColumn = j + offset;
                    Piece piece = switch (c) {
                        case 'p' -> new Pawn(Player.black, new Position(i, offsetColumn));
                        case 'r' -> new Rook(Player.black, new Position(i, offsetColumn));
                        case 'n' -> new Knight(Player.black, new Position(i, offsetColumn));
                        case 'b' -> new Bishop(Player.black, new Position(i, offsetColumn));
                        case 'q' -> new Queen(Player.black, new Position(i, offsetColumn));
                        case 'k' -> new King(Player.black, new Position(i, offsetColumn));
                        case 'P' -> new Pawn(Player.white, new Position(i, offsetColumn));
                        case 'R' -> new Rook(Player.white, new Position(i, offsetColumn));
                        case 'N' -> new Knight(Player.white, new Position(i, offsetColumn));
                        case 'B' -> new Bishop(Player.white, new Position(i, offsetColumn));
                        case 'Q' -> new Queen(Player.white, new Position(i, offsetColumn));
                        case 'K' -> new King(Player.white, new Position(i, offsetColumn));
                        default -> {
                            throw new IllegalStateException("Unexpected value: " + c);
                        }
                    };

                    if (piece != null) {
                        if (piece.getPlayer() == Player.white) {
                            whitePieces.add(piece);
                        } else {
                            blackPieces.add(piece);
                        }
                    }
                }
            }
        }
    }

    public String translateToFEN() {
        StringBuilder stringBuilder = new StringBuilder();
        List<Piece> sortedPieces = Stream.concat(whitePieces.parallelStream(), blackPieces.parallelStream())
                .sorted((p1, p2) -> {
                    var pos1 = p1.getPosition();
                    var pos2 = p2.getPosition();

                    int val = 0;
                    if (pos1.row() > pos2.row()) {
                        val = 1;
                    } else if (pos1.row() < pos2.row()) {
                        val = -1;
                    }
                    return val;
                })
                .collect(Collectors.toList());

        var piece = sortedPieces.get(0);
        for (int i = 0; i < 8; i++) {
            int offset = 0;
            for (int j = 0; j < 8; j++) {
                var position = piece.getPosition();
                if (position.row() == i && position.column() == j) {
                    if (offset != 0) {
                        stringBuilder.append(offset);
                        offset = 0;
                    }
                    var c = translatePiece(piece);
                    stringBuilder.append(c);
                    sortedPieces.remove(0);
                    if (!sortedPieces.isEmpty()) {
                        piece = sortedPieces.get(0);
                    }
                } else {
                    offset++;
                }
            }
            if (offset != 0) {
                stringBuilder.append(offset);
            }

            if (i != 7) {
                stringBuilder.append("/");
            }
        }
        stringBuilder.append(" - - - - -");
        return stringBuilder.toString();
    }

    private char translatePiece(Piece piece) {
        char c = switch (piece) {
            case Pawn b -> 'p';
            case Rook b -> 'r';
            case Knight b -> 'n';
            case Bishop b -> 'b';
            case King b -> 'k';
            case Queen b -> 'q';
            default -> throw new IllegalStateException("Unexpected value: " + piece);
        };

        if (piece.getPlayer() == Player.white) {
            c = Character.toUpperCase(c);
        }
        return c;
    }
}