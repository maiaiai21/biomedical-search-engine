package service;

import model.Paper;
import java.util.*;

public class IDFCache { //INVERSE DOCUMENT FREQUENCY
    private final Map<String, Long> termDocCount = new HashMap<>(); //no. of papers containing the word
    private final int totalDocs; //total no. of docs

    public IDFCache(List<Paper> papers) {
        this.totalDocs = papers.size();
        for (Paper p : papers)
            for (String term : p.wordCounts.keySet())
                termDocCount.merge(term, 1L, Long::sum); //1L - L IS LONG
    }

    public double idf(String term) {
        long df = termDocCount.getOrDefault(term, 0L);
        return Math.log((double) totalDocs / (1 + df)); //FORMULA FOR IDF
    }
}