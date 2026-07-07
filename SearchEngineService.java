package service;

import model.Paper;
import utils.Tokenizer;
import java.util.*;

public class SearchEngineService {

    public double calculateSearchScore(String query, Paper paper, IDFCache cache) {
        List<String> queryTerms = Tokenizer.tokenize(query);
        double totalScore = 0;

        for (String term : queryTerms) {
            if (paper.wordCounts.containsKey(term)) {
                double tf  = (double) paper.wordCounts.get(term) / paper.totalTokens; //term frequency
                double idf = cache.idf(term); //inverse document frequency
                totalScore += tf * idf;
            } else {
                for (String wordInPaper : paper.wordCounts.keySet()) {
                    if (wordInPaper.contains(term)) {
                        double tf = (double) paper.wordCounts.get(wordInPaper) / paper.totalTokens;
                        totalScore += tf * 0.4;
                        break;
                    }
                }
            }
        }
        return totalScore;
    }

    public double calculateSimilarity(Paper p1, Paper p2) { //cosine similarity, papers are vectors
        Set<String> allTerms = new HashSet<>(p1.wordCounts.keySet());
        allTerms.addAll(p2.wordCounts.keySet());

        double dotProduct = 0, normA = 0, normB = 0;
        for (String term : allTerms) {
            double v1 = tfidfWeight(term, p1);
            double v2 = tfidfWeight(term, p2);
            dotProduct += v1 * v2;
            normA += v1 * v1;
            normB += v2 * v2;
        }
        return (normA == 0 || normB == 0) ? 0 : dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double tfidfWeight(String term, Paper paper) { //adding weight to terms
        int count = paper.wordCounts.getOrDefault(term, 0);
        if (count == 0 || paper.totalTokens == 0) return 0.0;
        double tf  = (double) count / paper.totalTokens;
        double idf = Math.log(1.0 + (double) paper.totalTokens / count);
        return tf * idf;
    }
}