package com.whitespace.ai.movement;

import com.whitespace.BestMoveService;
import com.whitespace.ChessBoard;
import com.whitespace.board.Move;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CachingBestMoveService implements BestMoveService {
    private final String cacheFileName = "best-move-cache.txt";
    private final Map<ChessBoard, Move> cache = new HashMap<>();

    private final BestMoveService bestMoveService;

    public CachingBestMoveService(BestMoveService bestMoveService) {
        this.bestMoveService = bestMoveService;
    }

    @Override
    public Optional<Move> findBestMove(ChessBoard chessBoard) {
        var move = cache.compute(chessBoard, (key, value) -> {
            var optimalMove = value;
            if (optimalMove == null) {
                var optional = bestMoveService.findBestMove(key);
                if (optional.isPresent()) {
                    optimalMove = cache.put(key, optional.get());
                }
            }
            return optimalMove;
        });
        return Optional.ofNullable(move);
    }

    public void writeToDisk() {
        System.out.println("Writing the cache to disk");
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
            System.out.println("Completed writing the cache to disk");
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
//                    String[] data = scanner.nextLine().split(":");
//                    var key = Integer.parseInt(data[0]);
//                    var value = Double.parseDouble(data[1]);
//                    cache.put(key, value);
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
}
