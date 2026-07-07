# Biomedical Research Paper Search Engine

A desktop search engine built in Java/JavaFX for retrieving and ranking biomedical research papers using classical information retrieval techniques — TF-IDF weighting, cosine similarity, and a medical-aware tokenizer — with a built-in explainability panel showing *why* each result was ranked the way it was.

## Overview

Most search tools return ranked results as a black box. This project focuses on transparency: alongside each search result, the explainability panel breaks down the term overlaps and similarity scores that produced the ranking, making the retrieval process interpretable rather than opaque.

## Features

- **TF-IDF Ranking** — Documents are scored using Term Frequency–Inverse Document Frequency weighting, so rare, informative terms carry more weight than common ones.
- **Cosine Similarity** — Query and document vectors are compared using cosine similarity to rank relevance.
- **Medical-Aware Tokenizer** — Custom tokenizer built to handle biomedical text: hyphenated clinical terms, abbreviations, chemical/drug names, and other domain-specific tokens that a generic tokenizer would mishandle.
- **Explainability Panel** — A UI panel showing the term-level contributions behind each result's score, so users can see *why* a paper was ranked above another.
- **JavaFX Desktop GUI** — Clean, responsive interface for querying and browsing results.

## Tech Stack

- **Language:** Java
- **UI Framework:** JavaFX
- **Core Techniques:** TF-IDF, Cosine Similarity, custom NLP tokenization

## How It Works

1. **Indexing** — Research papers (title, abstract, body text) are tokenized using the medical-aware tokenizer and indexed into a term-document matrix.
2. **Query Processing** — User queries are tokenized using the same pipeline to ensure consistent term matching.
3. **Scoring** — Each document is scored against the query using TF-IDF-weighted cosine similarity.
4. **Ranking & Display** — Results are ranked by similarity score and displayed in the JavaFX interface.
5. **Explainability** — For any result, the panel shows which query terms matched, their TF-IDF weights, and their contribution to the final similarity score.

## Prerequisites
- JDK 17+ (or your installed version)
- JavaFX SDK configured in your IDE/module path

## Dataset

Uses a subset of the **PubMed** research paper dataset (`pubmed_small.csv`), sourced from Kaggle, containing paper metadata used to build the searchable index.

## Project Structure

```
AISearchEngine/
├── src/
│   ├── app/
│   │   └── SearchApp.java              # JavaFX entry point / main application window
│   ├── model/
│   │   └── Paper.java                          # Paper data model
│   ├── service/
│   │   ├── CSVLoader.java                   # Loads and parses pubmed_small.csv
│   │   ├── ExplainabilityService.java  # Generates term-level score breakdowns
│   │   ├── FilterService.java                 # Result filtering logic
│   │   ├── IDFCache.java                       # Caches IDF values across the corpus
│   │   ├── RecentSearchService.java   # Tracks recent search history
│   │   └── SearchEngineService.java   # Core TF-IDF + cosine similarity ranking
│   └── utils/
│       └── Tokenizer.java                       # Medical-aware tokenizer
├── pubmed_small.csv                          # Dataset
└── README.md
```

## Running the Project

Entry point: `src/app/SearchApp.java`

```bash
# Clone the repository
# Compile and run (adjust paths to your JavaFX SDK)
javac --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml -d out src/app/*.java src/model/*.java src/service/*.java src/utils/*.java
java --module-path <path-to-javafx-sdk>/lib --add-modules javafx.controls,javafx.fxml -cp out app.SearchApp
```

## Future Improvements

- Add support for additional similarity metrics (BM25) for comparison against TF-IDF
- Expand tokenizer coverage for multi-word clinical entities
- Add persistent indexing so re-runs don't require rebuilding the index from scratch
