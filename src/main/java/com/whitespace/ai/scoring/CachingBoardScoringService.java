package com.whitespace.ai.scoring;

import com.whitespace.BoardScoreService;
import com.whitespace.ChessBoard;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CachingBoardScoringService implements BoardScoreService {
    private final Map<ChessBoard, Double> treeCache = new TreeMap<>((o1, o2) -> {
        CompareToBuilder compareToBuilder = new CompareToBuilder();
        compareToBuilder.append(o1.getPieces(), o2.getPieces());
        var comp = compareToBuilder.toComparison();
        return comp;
    });

    private final Map<Integer, Double> hashMap = new HashMap<>();

    private long hits = 0;
    private long misses = 0;

    private final BoardScoreService boardScoreService;

    public CachingBoardScoringService(BoardScoreService boardScoreService) {
        this.boardScoreService = boardScoreService;
    }

    @Override
    public double scoreBoard(ChessBoard chessBoard) {
        printStats();
//        useTreeMap(chessBoard);
        return useHashMap(chessBoard);
    }

    private Double useHashMap(ChessBoard chessBoard) {
        int hashCode = chessBoard.hashCode();
        return hashMap.compute(hashCode, (integer, score) -> {
            if (score == null) {
                misses++;
                score = boardScoreService.scoreBoard(chessBoard);
            } else {
                hits++;
            }
            return score;
        });
    }

    private Double useTreeMap(ChessBoard chessBoard) {
        return treeCache.compute(chessBoard, (key, score) -> {
            if (score == null) {
                misses++;
                score = boardScoreService.scoreBoard(key);
            } else {
                hits++;
            }
            return score;
        });
    }

    public void printStats() {
        double total = hits + misses;
        int threshhold = 10000;
        if (total % threshhold == 0 && total >= threshhold) {
            double stats = hits / total;
            String format = "Stats: hits = [%s] misses = [%s] hit rate = [%s]";
            System.out.println(String.format(format, hits, misses, stats));
        }
    }

//    public void writeToDisk() {
//        if (cache.size() % (10000 * timesWrittenToDisk) == 0) {
//            printStats();
//            System.out.println("Writing " + cache.size() + " to disk");
//            try {
//                var fileWriter = new FileWriter(cacheFileName);
//                cache.forEach((key, value) -> {
//                    try {
//                        fileWriter.write(key + ":" + value + System.lineSeparator());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                });
//                fileWriter.close();
//            } catch (IOException e) {
//                System.out.println("An error occurred.");
//            } finally {
//                timesWrittenToDisk++;
//                System.out.println("Completed writing the cache to disk");
//            }
//        }
//    }

//    public void loadFromDisk() {
//        var file = new File(cacheFileName);
//        if (file.exists()) {
//            System.out.println("Loading the cache file");
//            Scanner scanner = null;
//            try {
//                scanner = new Scanner(file);
//                while (scanner.hasNextLine()) {
//                    String[] data = scanner.nextLine().split(":");
//                    var key = Integer.parseInt(data[0]);
//                    var value = Double.parseDouble(data[1]);
//                    cache.put(key, value);
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                if (scanner != null) {
//                    scanner.close();
//                }
//                System.out.println("Cache file load complete");
//            }
//        }
//    }
}
