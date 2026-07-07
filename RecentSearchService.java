package service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class RecentSearchService {
    private static final String FILE_NAME = "recent_searches.txt";
    private static final int MAX = 5;

    public void save(String query) {
        if (query == null || query.isBlank()) return;
        List<String> existing = load();
        existing.remove(query); // Remove if exists to move to top (LEAST RECENTLY USED LOGIC)
        existing.add(0, query);
        
        if (existing.size() > MAX) existing = existing.subList(0, MAX);
        
        try {
            // Using a simple PrintWriter for better compatibility across environments
            try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) { //printwriter to write into file
                for (String s : existing) out.println(s); //binary, text file u have to stack function unlike python
            }
        } catch (IOException e) {
            System.err.println("Could not save recent searches: " + e.getMessage());
        }
    }

    public List<String> load() {
        List<String> lines = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return lines;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) lines.add(line);
            }
        } catch (FileNotFoundException e) {
            return lines;
        }
        return lines;
    }
}