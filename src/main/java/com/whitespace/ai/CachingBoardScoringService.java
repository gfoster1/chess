package com.whitespace.ai;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import com.whitespace.movement.Position;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class CachingBoardScoringService implements BoardScoreService {
    private final Map<Integer, Double> cache = new TreeMap<>();

    private final BoardScoreService boardScoreService;

    public CachingBoardScoringService(BoardScoreService boardScoreService) {
        this.boardScoreService = boardScoreService;
        loadFromDisk();
    }

    @Override
    public double scoreBoard(ChessBoard chessBoard) {
        var hashCodeBuilder = new HashCodeBuilder();
        chessBoard.getPieces().parallelStream().forEach(piece -> {
            hashCodeBuilder.append(piece.getClass());
            Position position = piece.getPosition();
            hashCodeBuilder.append(position.row());
            hashCodeBuilder.append(position.column());
            hashCodeBuilder.append(piece.getPlayer());
        });
        int hashCode = hashCodeBuilder.toHashCode();

        if (cache.containsKey(hashCode)) {
            return cache.get(hashCode);
        }

        var score = boardScoreService.scoreBoard(chessBoard);
        cache.put(hashCode, score);
        return score;
    }

    public void writeToDisk() {
        System.out.println("Writing the cache to disk");
        try {
            var fileWriter = new FileWriter("cache.txt");
            cache.forEach((key, value) -> {
                try {
                    fileWriter.write(key + ":" + value + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
        } finally {
            System.out.println("Completed writing the cache to disk");
        }
    }

    public void loadFromDisk() {
        var file = new File("cache.txt");
        if (!file.exists()) {
            return;
        }

        System.out.println("Loading the cache file");
        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(":");
                var key = Integer.parseInt(data[0]);
                var value = Double.parseDouble(data[1]);
                cache.put(key, value);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
                System.out.println("Cache file load complete");
            }
        }
    }
}
