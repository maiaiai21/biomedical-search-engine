package utils;

import java.util.*;
import java.util.stream.Collectors;

public class Tokenizer {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "and", "or", "in", "on", "at", "to", "is", "for",
        "with", "of", "it", "that", "this", "was", "as", "are", "be", "been",
        "were", "by", "from", "has", "had", "have", "not", "but", "we", "our",
        "their", "which", "also", "can", "may", "between", "into", "than",
        "after", "before", "during", "while", "used", "using", "based"
    ));

    public static List<String> tokenize(String text) {
        if (text == null) return new ArrayList<>();
        String[] rawTokens = text.split("[\\s,;:()\\[\\]\"'/]+");
        return Arrays.stream(rawTokens)
            .filter(w -> w.length() > 1)
            .map(String::toLowerCase)
            .filter(w -> !STOP_WORDS.contains(w))
            .map(Tokenizer::smartStem)
            .filter(w -> w.length() > 1)
            .collect(Collectors.toList());
    }

    private static String smartStem(String word) {
        if (isMedicalTerm(word)) return word;
        if (isLikelyAcronym(word)) return word;
        return stem(word);
    }

    private static boolean isMedicalTerm(String word) {
        if (word.matches(".*\\d+.*")) return true;
        if (word.contains("-")) return true;
        if (word.matches(".*(alpha|beta|gamma|delta|omega|kappa|sigma).*")) return true;
        return false;
    }

    private static boolean isLikelyAcronym(String word) {
        Set<String> bioAcronyms = new HashSet<>(Arrays.asList(
            "dna", "rna", "mrna", "pcr", "mri", "ct", "hiv", "hpv",
            "icu", "er", "bp", "bmi", "cns", "pns", "ecg", "eeg",
            "fda", "who", "cdc", "nih", "snp", "gwas", "ace", "atp"
        ));
        return bioAcronyms.contains(word);
    }

    private static String stem(String word) {
        if (word.length() <= 3) return word;
        if (word.endsWith("ization")) return word.substring(0, word.length() - 7);
        if (word.endsWith("ations"))  return word.substring(0, word.length() - 6);
        if (word.endsWith("tion"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("ness"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("ment"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("ing"))     return word.substring(0, word.length() - 3);
        if (word.endsWith("ical"))    return word.substring(0, word.length() - 4);
        if (word.endsWith("ied"))     return word.substring(0, word.length() - 3) + "y";
        if (word.endsWith("ies"))     return word.substring(0, word.length() - 3) + "y";
        if (word.endsWith("ed"))      return word.substring(0, word.length() - 2);
        if (word.endsWith("ly"))      return word.substring(0, word.length() - 2);
        if (word.endsWith("es") && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && word.length() > 4 && !word.endsWith("ss"))
            return word.substring(0, word.length() - 1);
        return word;
    }
}