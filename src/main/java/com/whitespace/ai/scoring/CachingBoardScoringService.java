package com.whitespace.ai.scoring;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class CachingBoardScoringService implements BoardScoreService {
    private final String cacheFileName = "cache.txt";
    private final Map<Integer, Double> cache = new ConcurrentHashMap<>();

    private long hits = 0;
    private long misses = 0;
    private int timesWrittenToDisk = 1;

    private final BoardScoreService boardScoreService;

    public CachingBoardScoringService(BoardScoreService boardScoreService) {
        this.boardScoreService = boardScoreService;
        loadFromDisk();
    }

    @Override
    public double scoreBoard(ChessBoard chessBoard) {
        int hashCode = chessBoard.hashCode();
        return cache.compute(hashCode, (key, score) -> {
            if (score == null) {
                score = boardScoreService.scoreBoard(chessBoard);
                misses++;
            } else {
                hits++;
            }
            writeToDisk();
            return score;
        });
    }

    private void printStats() {
        double total = hits + misses;
        double stats = hits / total;
        String format = "Stats: hits = [%s] misses = [%s] hit rate = [%s]";
        System.out.println(String.format(format, hits, misses, stats));
    }

    public void writeToDisk() {
        if (cache.size() % (10000 * timesWrittenToDisk) == 0) {
            printStats();
            System.out.println("Writing " + cache.size() + " to disk");
            try {
                var fileWriter = new FileWriter(cacheFileName);
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
                timesWrittenToDisk++;
                System.out.println("Completed writing the cache to disk");
            }
        }
    }

    public void loadFromDisk() {
        var file = new File(cacheFileName);
        if (file.exists()) {
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
                }
                System.out.println("Cache file load complete");
            }
        }
    }
}
