# LangChain4j with Ollama — Example for Mark Watson's book "Practical Artificial Intelligence With Java"

Book URI: https://leanpub.com/javaai

You can read my book for free online at: https://leanpub.com/javaai/read

This example shows how to use the [LangChain4j](https://docs.langchain4j.dev) library as an abstraction layer for different LLM providers. Here it is wired to a locally-running Ollama server, demonstrating that you can swap LLM backends (OpenAI, Gemini, Ollama, etc.) with minimal code changes thanks to LangChain4j's unified API.

## Prerequisites

- Java 11+
- Maven 3.6+
- [Ollama](https://ollama.com) installed with a model pulled (e.g., `ollama pull llama3`)

## Setup

Make sure Ollama is running before executing the example:

```bash
ollama serve
```

## Build & Run

```bash
# Run via the Makefile
make

# Or manually
mvn test -q
```

## Key Dependencies

| Artifact | Purpose |
|---|---|
| `dev.langchain4j:langchain4j` | LangChain4j core abstraction |
| `dev.langchain4j:langchain4j-open-ai` | OpenAI-compatible provider (used for Ollama's API) |

## Book Cover Material, Copyright, and License

This example is released using the Apache 2 license.

Copyright 2022-2026 Mark Watson. All rights reserved.

## This Book is Licensed with Creative Commons Attribution CC BY Version 3

You are free to share and adapt this content, with attribution.