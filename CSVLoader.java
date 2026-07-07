package service;

import model.Paper;
import java.io.*;
import java.util.*;

public class CSVLoader {
    
    public static List<Paper> load(String path) { //path=file path
        List<Paper> list = new ArrayList<>(); //container to hold parsed papers
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine();
            if (header == null) return list;

            String[] cols = parseCSVLine(header); //extracts column names
            Map<String, Integer> idx = new HashMap<>(); //link col name to pos
            for (int i = 0; i < cols.length; i++)
                idx.put(cols[i].trim().toLowerCase(), i);

            // Mapping Kaggle columns
            int titleIdx    = idx.getOrDefault("title", -1);
            int abstractIdx = idx.getOrDefault("abstracttext", -1);
            int meshIdx     = idx.getOrDefault("meshmajor", -1);
            int pmidIdx     = idx.getOrDefault("pmid", -1);

            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] v = parseCSVLine(line);
                    String title = get(v, titleIdx);
                    if (title.isEmpty()) continue; //skip record if empty

                    list.add(new Paper(
                        title, 
                        get(v, abstractIdx), 
                        get(v, meshIdx).replace("[", "").replace("]", "").replace("'", ""),
                        "PubMed", 
                        get(v, pmidIdx)
                    ));
                } catch (Exception e) { } //skip if error in parsing one record
            }
        } catch (Exception e) {
            System.err.println("CSV Load Error: " + e.getMessage());
        }
        return list;
    }

    private static String get(String[] arr, int idx) {
        if (idx < 0 || idx >= arr.length) return "";
        return arr[idx].replaceAll("^\"|\"$", "").trim(); //trimming the value, replacing symbols
    }

    private static String[] parseCSVLine(String line) { //csv values seperated by comma , quotes are there if comma there in title
        List<String> fields = new ArrayList<>(); //PROCESSED DATASET
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"'); i++;
                } else inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) { //cuts data when outside quotes and meets with comma
                fields.add(current.toString());
                current.setLength(0);
            } else current.append(c);
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}