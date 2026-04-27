# Natural Language Processing — Example for Mark Watson's book "Practical Artificial Intelligence With Java"

Book URI: https://leanpub.com/javaai

You can read my book for free online at: https://leanpub.com/javaai/read

This example provides a lightweight NLP toolkit written from scratch in Java. It includes named entity recognition using bundled name lists, a part-of-speech tagger (`FastTag`), an automatic keyword/topic extractor (`AutoTagger`), a text tokenizer, noise-word filtering, and a Porter stemmer. No external NLP service or large model download is required.

## Prerequisites

- Java 8+
- Maven 3.6+

## Build & Run

```bash
# Named entity recognition demo
make names

# Automatic keyword extraction demo
make autotagger

# Part-of-speech tagging demo
make fasttag
```

Or manually:

```bash
mvn install -DskipTests -q
mvn exec:java -Dexec.mainClass="com.markwatson.nlp.ExtractNames" -q
```

## Project Structure

- **`ExtractNames.java`** — Named entity recognition using serialized name databases (`test_data/propernames.ser`)
- **`FastTag.java`** — Rule-based part-of-speech tagger
- **`AutoTagger.java`** — Automatic keyword/topic extractor using TF-IDF-style scoring
- **`ComparableDocument.java`** — Document similarity comparison
- **`util/`** — Tokenizer, noise-word filter, scored lists, and other helpers

> **Note:** The file `test_data/propernames.ser` is a serialized Java object containing first names, last names, and place names. On first run, the `ExtractNames` constructor writes these out to human-readable `.txt` files for inspection.

## Book Cover Material, Copyright, and License

This example is released using the Apache 2 license.

Copyright 2022-2026 Mark Watson. All rights reserved.

## This Book is Licensed with Creative Commons Attribution CC BY Version 3

You are free to share and adapt this content, with attribution.
