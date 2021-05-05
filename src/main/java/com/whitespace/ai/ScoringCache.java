package com.whitespace.ai;

import com.whitespace.Board;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ScoringCache {
    private final Map<Integer, Integer> cache = new HashMap<>();

    public ScoringCache() {
        loadFromDisk();
    }

    public Optional<Integer> getBoardScore(Board board) {
        var hashCodeBuilder = new HashCodeBuilder();
        board.getPieces().parallelStream().forEach(piece -> {
            hashCodeBuilder.append(piece.getClass());
            hashCodeBuilder.append(piece.getPosition());
        });
        int hashCode = hashCodeBuilder.toHashCode();
        if (cache.containsKey(hashCode)) {
            return Optional.ofNullable(cache.get(hashCode));
        }

        return Optional.empty();
    }

    public void put(Board board, int score) {
        var hashCodeBuilder = new HashCodeBuilder();
        board.getPieces().parallelStream().forEach(piece -> {
            hashCodeBuilder.append(piece.getClass());
            hashCodeBuilder.append(piece.getPosition());
        });
        int hashCode = hashCodeBuilder.toHashCode();
        cache.put(hashCode, score);
    }

    public void writeToDisk() {
        try {
            var fileWriter = new FileWriter("cache.txt");
            cache.forEach((integer, integer2) -> {
                try {
                    fileWriter.write(integer + ":" + integer2 + System.lineSeparator());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            fileWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
        }
    }

    public void loadFromDisk() {
        var file = new File("cache.txt");
        if (!file.exists()) {
            return;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(":");
                int key = Integer.parseInt(data[0]);
                int value = Integer.parseInt(data[1]);
                cache.put(key, value);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}
