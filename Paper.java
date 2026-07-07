package model;

import java.util.*;
import utils.Tokenizer;

public class Paper {
    private String title, abstractText, journal, link, meshTerms, pmid;
    public Map<String, Integer> wordCounts = new HashMap<>();
    public int totalTokens = 0;
    public double score = 0.0;

    public Paper(String title, String abstractText, String meshTerms,
                 String journal, String pmid) {
        this.title        = title;
        this.abstractText = abstractText;
        this.meshTerms    = meshTerms;
        this.journal      = journal;
        this.pmid = pmid.trim().replaceAll("\\.0$", ""); 
        this.link = "https://pubmed.ncbi.nlm.nih.gov/" + this.pmid + "/";

        String fullText = title + " " + abstractText + " " + meshTerms;
        List<String> tokens = Tokenizer.tokenize(fullText);
        // Safety: ensure totalTokens is at least 1 to avoid division by zero
        this.totalTokens = Math.max(tokens.size(), 1); 
        for (String token : tokens)
            wordCounts.put(token, wordCounts.getOrDefault(token, 0) + 1);
    }

    public String getTitle()        { return title; }
    public String getAbstractText() { return abstractText; }
    public String getMeshTerms()    { return meshTerms; }
    public String getJournal()      { return journal; }
    public String getLink()         { return link; }
    public String getPmid()         { return pmid; }

    @Override
    public String toString() { return title; }
}