package utils;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class Utils {
    // Download image from URL and save to fileName
    public static void downloadImage(String imageUrl, String fileName) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try (InputStream in = new URL(imageUrl).openStream()) {
            Files.copy(in, new File(fileName).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to download image: " + imageUrl + " - " + e.getMessage());
        }
    }

    // Analyze repeated words in a list of headers (words repeated more than twice)
    public static Map<String, Integer> repeatedWordAnalysis(List<String> headers) {
        Map<String, Integer> wordCount = new HashMap<>();
        for (String header : headers) {
            String[] words = header.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
            for (String word : words) {
                if (word.isEmpty()) continue;
                wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
            }
        }
        Map<String, Integer> repeated = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > 2) {
                repeated.put(entry.getKey(), entry.getValue());
            }
        }
        return repeated;
    }
    
    // Analyze repeated words in a single text content
    public static Map<String, Integer> analyzeRepeatedWords(String content) {
        Map<String, Integer> wordCount = new HashMap<>();
        String[] words = content.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");
        for (String word : words) {
            if (word.isEmpty()) continue;
            wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
        }
        Map<String, Integer> repeated = new HashMap<>();
        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > 2) {
                repeated.put(entry.getKey(), entry.getValue());
            }
        }
        return repeated;
    }
} 