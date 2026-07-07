package service;

import model.Paper;
import java.util.*;
import java.util.stream.Collectors;

public class FilterService {

    private final SearchEngineService engine;
    private final List<Paper> masterData;
    private final IDFCache idfCache;

    public FilterService(SearchEngineService engine, List<Paper> masterData) { 
        this.engine     = engine;//connects the math
        this.masterData = masterData; //keeps reference of all papers
        this.idfCache   = new IDFCache(masterData); //analyse rarity of term
    }

    public IDFCache getIDFCache() { return idfCache; }

    public List<Paper> filter(String query, String ignoredYear, String sortOption) {
        List<Paper> filtered = new ArrayList<>(masterData); //fresh shallow copy of data to reorder search results

        if (query != null && !query.isEmpty()) {
            for (Paper p : filtered) {
                // Scoring logic remains the same to provide relevance
                p.score = Math.min(engine.calculateSearchScore(query, p, idfCache) * 12.0, 1.0);
            }
        } else {
            filtered.forEach(p -> p.score = 0.0);
        }

        // Sorting is to Relevance or Alphabetical
        if ("Title (A-Z)".equals(sortOption)) {
            filtered.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        } else {
            // Default to Relevance sorting based on TF-IDF score
            filtered.sort((a, b) -> Double.compare(b.score, a.score));
        }

        return filtered;
    }
}