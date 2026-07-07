package service;

import model.Paper;
import utils.Tokenizer;
import java.util.*;

public class ExplainabilityService {

    // A Record of the score of the word
    public record TermScore(String queryTerm, String matchedTerm, double tf, double idf, double score, boolean isPartialMatch) {}

    public List<TermScore> explain(String query, Paper paper, IDFCache cache) { //tokenises search term/ paper title and creates score term
        return Tokenizer.tokenize(query).stream()
            .map(term -> createScore(term, paper, cache))
            .filter(ts -> ts.score() > 0)
            .sorted((a, b) -> Double.compare(b.score(), a.score())) //sorts with highest score on top
            .toList();
    }

    private TermScore createScore(String term, Paper paper, IDFCache cache) {
        double idf = cache.idf(term);
        
        // Check for exact match first
        if (paper.wordCounts.containsKey(term)) {
            double tf = (double) paper.wordCounts.get(term) / paper.totalTokens;
            return new TermScore(term, term, tf, idf, tf * idf, false);
        }

        // Search for partial matches using Streams - sequence of elements
        return paper.wordCounts.keySet().stream()
            .filter(word -> word.contains(term))
            .findFirst()
            .map(matched -> {
                double tf = ((double) paper.wordCounts.get(matched) / paper.totalTokens) * 0.4; //0.4 as penalty since partial
                return new TermScore(term, matched, tf, idf, tf * idf, true);
            })
            // Return a zero-score object if no match is found
            .orElse(new TermScore(term, term, 0, idf, 0, false));
    }
}