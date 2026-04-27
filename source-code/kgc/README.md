# Knowledge Graph Creator — Example for Mark Watson's book "Practical Artificial Intelligence With Java"

Book URI: https://leanpub.com/javaai

You can read my book for free online at: https://leanpub.com/javaai/read

This example automatically generates RDF knowledge graph data from plain-text input. It uses named entity recognition (from the `ner_dbpedia` library) and Apache OpenNLP to extract people, places, and organizations from text, then emits RDF triples that link them via semantic relationships. The output is a deduplicated RDF file suitable for loading into a triplestore or the Semantic Web example.

## Prerequisites

- Java 11+
- Maven 3.6+
- **You must build and install the `ner_dbpedia` project first** (it is a local Maven dependency):

```bash
cd ../ner_dbpedia
make install
```

## Build & Run

```bash
# Build, run, and deduplicate output
make create_data_and_remove_duplicates

# Or step by step
mvn install -DskipTest -Dmaven.test.skip=true -q
mvn test -q
```

The output file `output.rdf` contains the generated knowledge graph.

## Key Dependencies

| Artifact | Purpose |
|---|---|
| `com.markwatson:nerdbpedia` | Named entity recognition + DBPedia linking (local) |
| `org.apache.opennlp:opennlp-tools` | NLP sentence detection and tokenization |

## Book Cover Material, Copyright, and License

This example is released using the Apache 2 license.

Copyright 2022-2026 Mark Watson. All rights reserved.

## This Book is Licensed with Creative Commons Attribution CC BY Version 3

You are free to share and adapt this content, with attribution.
