---
name: java-ai-dev
description: Java AI tutorial, APIs, and project reference for all examples in Mark Watson's book "Practical Artificial Intelligence With Java". Use this skill for writing Java code that accesses LLMs (Gemini, OpenAI, Ollama), SPARQL queries, NLP, web scraping, knowledge graphs, search algorithms, neural networks, genetic algorithms, and multi-agent frameworks.
---

# Notes for Using AGENT Skills with Java AI Book Examples

This document helps readers set up coding agent skills so that AI assistants can reference the Java APIs and patterns from this book when generating code.

## Source code for Gemini, OpenAI, Ollama, SPARQL, NLP, knowledge graphs, search, neural networks, and agent examples

```bash
git clone https://github.com/markwatson/Java-AI-Book.git
```

All Java examples are in the `source-code/` directory. Look in ~/GITHUB/Java-AI-Book/source-code/ for code to reuse.

---

## Java Project Conventions

All examples in this book are standard **Maven** Java projects. Each example directory contains a `pom.xml` and follows the Maven `src/main/java/` and `src/test/java/` layout.

### Build & Run Pattern

```bash
cd source-code/<example_name>

# Most projects: build and run via Makefile
make

# Or manually with Maven
mvn install -DskipTests -q   # compile & install to local repo
mvn test -q                  # run unit tests / demos
```

### Common Utility Pattern

Most LLM client classes share a common set of utility methods:

```java
// Read a file into a String
public static String readFileToString(String filePath) throws IOException

// Simple string replacement
public static String replaceSubstring(String original, String toReplace, String replacement)

// Substitute a variable in a prompt template
public static String promptVar(String prompt, String varName, String varValue)
```

### Dependencies

All projects use Maven for dependency management. Some projects are local Maven dependencies that must be installed first (e.g., `ner_dbpedia`, `semantic_web_apache_jena`).

### Environment Variables

| Variable | Used By |
|---|---|
| `GOOGLE_API_KEY` | `gemini-llm-client` |
| `GEMINI_API_KEY` | `AgentScope_gemini` |
| `OPENAI_API_KEY` | `openai-llm-client`, `langchain4j-ollama` |

---

# Java AI Book APIs — Quick Reference

Knowledge of public APIs and usage patterns for the Java examples in Mark Watson's book *Practical Artificial Intelligence With Java*.

---

## gemini-llm-client

**Directory:** `gemini-llm-client/`
**Package:** `com.markwatson.gemini`
**Deps:** `org.json:json`
**Env var:** `GOOGLE_API_KEY`
**Model:** `gemini-3-flash-preview`

### API — `GeminiCompletions`

- `static String getCompletion(String prompt)` — Send a prompt to the Gemini REST API. Returns the text response.
- `static String getCompletionWithSearch(String prompt)` — Send a prompt with `google_search` tool enabled (grounded web search). Returns the text response.
- `static String readFileToString(String filePath)` — Read a file into a String (escapes double quotes).
- `static String promptVar(String prompt, String varName, String varValue)` — Substitute a variable in a prompt template.

### Example

```java
import com.markwatson.gemini.GeminiCompletions;

String answer = GeminiCompletions.getCompletion("How much is 11 + 22?");
System.out.println(answer);

// With web search grounding
String news = GeminiCompletions.getCompletionWithSearch("What is the latest news about Mars?");
System.out.println(news);
```

---

## openai-llm-client

**Directory:** `openai-llm-client/`
**Package:** `com.markwatson.openai`
**Deps:** `org.json:json`
**Env var:** `OPENAI_API_KEY`
**Model:** `gpt-4o-mini`

### API — `OpenAICompletions`

- `static String getCompletion(String prompt)` — Send a chat completion request to the OpenAI API. Returns the text response.

### Example

```java
import com.markwatson.openai.OpenAICompletions;

String result = OpenAICompletions.getCompletion("Explain recursion briefly");
System.out.println(result);
```

---

## ollama-llm-client

**Directory:** `ollama-llm-client/`
**Package:** `com.markwatson.ollama`
**Deps:** `org.json:json`
**Server:** Requires Ollama running locally on `http://localhost:11434`

### API — `OllamaLlmClient`

- `static String getCompletion(String prompt, String modelName)` — Send a prompt to a local Ollama model. Returns the text response.

### Example

```java
import com.markwatson.ollama.OllamaLlmClient;

String answer = OllamaLlmClient.getCompletion("What is Java?", "mistral");
System.out.println(answer);
```

---

## langchain4j-ollama

**Directory:** `langchain4j-ollama/`
**Package:** `com.markwatson.langchain4j_ollama`
**Deps:** `dev.langchain4j:langchain4j`, `dev.langchain4j:langchain4j-open-ai`, `org.json:json`
**Env var:** `OPENAI_API_KEY`
**Server:** Requires Ollama running locally

### API — `OllamaLlmLangChain4j`

- `static String getCompletion(String prompt, String modelName)` — Send a prompt via LangChain4j's unified API. Returns the text response.

### Example

```java
import com.markwatson.langchain4j_ollama.OllamaLlmLangChain4j;

String result = OllamaLlmLangChain4j.getCompletion("Translate 'Hello' to French", "mistral");
System.out.println(result);
```

---

## AgentScope_gemini

**Directory:** `AgentScope_gemini/`
**Package:** `com.markwatson.agentscope`
**Deps:** `io.agentscope:agentscope:1.0.9`, `com.google.genai:google-genai`
**Env var:** `GEMINI_API_KEY`
**Model:** `gemini-2.5-flash`
**Requires:** Java 17+

### Key Classes

- **`Main.java`** — Basic ReActAgent demo: build a Gemini model, create a ReActAgent, send a message.
- **`ToolUseExample.java`** — Tool-calling demo: registers `@Tool`-annotated methods (weather stub, filesystem tools) with a `Toolkit`, then attaches to a ReActAgent.

### Example — Basic Agent

```java
import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.GeminiChatModel;

GeminiChatModel model = GeminiChatModel.builder()
        .apiKey(System.getenv("GEMINI_API_KEY"))
        .modelName("gemini-2.5-flash")
        .build();

ReActAgent agent = ReActAgent.builder()
        .name("Assistant")
        .sysPrompt("You are a helpful AI assistant.")
        .model(model)
        .build();

Msg response = agent.call(
        Msg.builder().textContent("Tell me a fun fact about Java.").build()
).block();
System.out.println(response.getTextContent());
```

### Example — Tool Use

```java
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

public static class WeatherService {
    @Tool(description = "Get the current weather for a specified city")
    public String getWeather(
            @ToolParam(name = "city", description = "The name of the city") String city) {
        return city + " weather: Sunny, 25°C";
    }
}

Toolkit toolkit = new Toolkit();
toolkit.registerTool(new WeatherService());

ReActAgent agent = ReActAgent.builder()
        .name("ToolAgent")
        .sysPrompt("You are a helpful assistant with access to weather data.")
        .model(model)
        .toolkit(toolkit)
        .build();
```

---

## nlp

**Directory:** `nlp/`
**Package:** `com.markwatson.nlp`
**Deps:** none (pure Java, bundled data files)

### Key Classes

- **`ExtractNames`** — Named entity recognition using serialized name databases.
  - `ScoredList[] getProperNames(String text)` — Returns arrays of `[human names, place names, ...]` extracted from text.
  - `boolean isHumanName(String name)` — Check if a name is a known human name.
  - `boolean isPlaceName(String name)` — Check if a name is a known place name.
- **`FastTag`** — Rule-based part-of-speech tagger.
- **`AutoTagger`** — Automatic keyword/topic extractor using TF-IDF-style scoring.
- **`util/Tokenizer`** — Text tokenizer and noise-word filter.

### Example

```java
import com.markwatson.nlp.ExtractNames;
import com.markwatson.nlp.util.ScoredList;

ExtractNames en = new ExtractNames("test_data");
ScoredList[] names = en.getProperNames("President Bush went to San Diego to meet Ms Jones at Google");
// names[0] = human names, names[1] = place names, ...
```

---

## ner_dbpedia

**Directory:** `ner_dbpedia/`
**Package:** `com.markwatson.ner_dbpedia`
**Deps:** none (pure Java, bundled data)
**Note:** This is a local Maven dependency — install with `make install` before building `kgc` or `kgn`.

### API — `TextToDbpediaUris`

Resolves named entities to DBpedia URIs. After construction with a text string, access these public fields:

- `List<String> personUris / personNames`
- `List<String> companyUris / companyNames`
- `List<String> cityUris / cityNames`
- `List<String> countryUris / countryNames`
- `List<String> broadcastNetworkUris / broadcastNetworkNames`
- `List<String> musicGroupUris / musicGroupNames`
- `List<String> politicalPartyUris / politicalPartyNames`
- `List<String> tradeUnionUris / tradeUnionNames`

### Example

```java
import com.markwatson.ner_dbpedia.TextToDbpediaUris;

TextToDbpediaUris uris = new TextToDbpediaUris("Bill Gates founded Microsoft in Redmond");
System.out.println("People: " + uris.personNames);
System.out.println("Companies: " + uris.companyNames);
```

---

## semantic_web_apache_jena

**Directory:** `semantic_web_apache_jena/`
**Package:** `com.markwatson.semanticweb`
**Deps:** `org.apache.jena:apache-jena-libs`, `org.apache.derby:derby`
**Note:** Local Maven dependency — install with `make install` before building `kgn`.

### API — `JenaApis`

- `void loadRdfFile(String fpath)` — Load an RDF file into the in-memory model.
- `void saveModelToTurtleFormat(String outputPath)` — Serialize model as Turtle.
- `void saveModelToN3Format(String outputPath)` — Serialize model as N3.
- `QueryResult query(String sparqlQuery)` — Execute a SPARQL query against the local model.
- `QueryResult queryRemote(String service, String sparqlQuery)` — Execute a SPARQL query against a remote endpoint (e.g., DBpedia). Results are cached in an embedded Derby database.

### Example

```java
import com.markwatson.semanticweb.JenaApis;
import com.markwatson.semanticweb.QueryResult;

JenaApis jena = new JenaApis();
QueryResult result = jena.queryRemote(
    "https://dbpedia.org/sparql",
    "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 5");
// result contains variable names and bindings
```

---

## kgn (Knowledge Graph Navigator)

**Directory:** `kgn/`
**Package:** `com.knowledgegraphnavigator`
**Deps:** `com.markwatson:nerdbpedia` (local), `com.markwatson:semanticweb` (local), `org.apache.jena:apache-jena-libs`
**Requires:** Build `ner_dbpedia` and `semantic_web_apache_jena` first.

### Key Classes

- **`KGN`** — Main interactive app: extracts entities from text, resolves to DBpedia URIs, runs SPARQL to find relationships.
- **`Sparql`** — Wrapper for remote SPARQL queries via the `JenaApis` library.
- **`EntityDetail`** / **`EntityRelationships`** / **`EntityAndDescription`** — Data classes for entity information.
- **`Utils`** — Shared utility methods.

### Build

```bash
cd ../ner_dbpedia && make install
cd ../semantic_web_apache_jena && make install
cd ../kgn && make install && mvn test
```

---

## kgc (Knowledge Graph Creator)

**Directory:** `kgc/`
**Package:** `com.markwatson.kgc`
**Deps:** `com.markwatson:nerdbpedia` (local), `org.apache.opennlp:opennlp-tools`
**Requires:** Build `ner_dbpedia` first.

### API — `KGC`

Generates RDF triples from plain text by extracting named entities and linking them via semantic relationships.

### Build

```bash
cd ../ner_dbpedia && make install
cd ../kgc && make create_data_and_remove_duplicates
```

---

## info_gathering

**Directory:** `info_gathering/`
**Package:** `com.markwatson.info_spiders`
**Deps:** `org.jsoup:jsoup`, `org.apache.opennlp:opennlp-tools`

### Key Classes

- **`WebSpider`** — Web scraping spider that follows links up to a configurable depth.
  - `WebSpider(String rootUrl, int maxPages)` — Constructor starts crawling immediately.
  - `List<List<String>> url_content_lists` — Results: each inner list is `[url, text_content]`.
- **`GeoNamesClient`** — REST client for the GeoNames geographical API.
- **`DBpediaLookupClient`** — REST client for DBpedia entity lookup.

### Example

```java
import com.markwatson.info_spiders.WebSpider;

WebSpider spider = new WebSpider("https://markwatson.com", 3);
for (List<String> page : spider.url_content_lists) {
    System.out.println("URL: " + page.get(0));
    System.out.println("Content: " + page.get(1).substring(0, 200));
}
```

---

## neuralnetworks

**Directory:** `neuralnetworks/`
**Package:** `com.markwatson.neuralnetworks`
**Deps:** none (pure Java with Swing GUI)

### Key Classes

- **`Neural_1H`** — Single hidden-layer backpropagation network.
- **`Neural_2H`** — Two hidden-layer backpropagation network.
- **`Neural_2H_momentum`** — Two hidden-layer network with momentum term.
- **`Graph`**, **`GraphPanel`**, **`Plot1DPanel`**, **`Plot2DPanel`** — Swing GUI for real-time training visualization.

### Build & Run

```bash
make 1H              # 1 hidden layer
make 2H              # 2 hidden layers
make 2H_momentum     # 2 hidden layers with momentum
```

---

## genetic-algorithms

**Directory:** `genetic-algorithms/`
**Package:** `com.markwatson.geneticalgorithms`
**Deps:** none (pure Java)

### Key Classes

- **`Genetic`** — Core GA framework: chromosome representation, fitness evaluation, crossover, and mutation.
- **`TestGenetic`** — Test harness that evolves a population toward a target solution.

### Build & Run

```bash
make
# Or: mvn test -q
```

---

## anomaly_detection

**Directory:** `anomaly_detection/`
**Package:** `com.markwatson.anomaly_detection`
**Deps:** none (pure Java)

### API — `AnomalyDetection`

- `AnomalyDetection(int numFeatures, int numTrainingExamples, double[][] trainingData)` — Constructor.
- `void train()` — Compute feature statistics and determine the optimal epsilon threshold.
- `boolean isAnamoly(double[] x)` — Test whether a data point is anomalous.
- `double[] muValues()` — Get the computed mean for each feature.
- `double[] sigmaSquared()` — Get the computed variance for each feature.
- `double bestEpsilon()` — Get the best epsilon threshold found during training.

### Example

```java
import com.markwatson.anomaly_detection.AnomalyDetection;

AnomalyDetection ad = new AnomalyDetection(numFeatures, numExamples, trainingData);
ad.train();
boolean isOutlier = ad.isAnamoly(newDataPoint);
System.out.println("Best epsilon: " + ad.bestEpsilon());
```

---

## search

**Directory:** `search/`
**Packages:** `search.graph`, `search.maze`, `search.game`
**Deps:** none (pure Java with Swing GUI)

### Key Classes

- **`search.graph`** — `AbstractGraphSearch`, `DepthFirstSearch`, `BreadthFirstSearch` with Swing visualizations.
- **`search.maze`** — `Maze`, `DepthFirstSearchEngine`, `BreadthFirstSearchEngine` with Swing visualizations.
- **`search.game`** — `GameSearch` (abstract), `TicTacToe`, `Chess` with alpha-beta pruning.

### Build & Run

```bash
make           # runs all demos
# Or individual demos via mvn exec:java
```

---

## prompts

**Directory:** `prompts/`
**No code** — contains prompt template text files used by the LLM client examples.

### Files

- `summarization_prompt.txt` — Summarization prompt template.
- `extraction_prompt.txt` — Information extraction prompt template.
- `two-shot-2.txt` / `two-shot-2-var.txt` — Few-shot prompting examples.

---

## General Notes

- All examples use **Maven** for dependency management; each has its own `pom.xml`.
- Build tool is Java's standard `javac` via Maven's `maven-compiler-plugin`.
- Java version requirements vary: most need **Java 11+**, AgentScope needs **Java 17+**.
- Several projects are local Maven dependencies that must be `mvn install`'d before dependent projects will compile: `ner_dbpedia`, `semantic_web_apache_jena`, `nlp`.
- The `prompts/` directory contains reusable prompt templates loaded via `readFileToString()`.
- REST API calls to LLM providers use `java.net.HttpURLConnection` directly (no SDK), except `AgentScope_gemini` which uses the Google GenAI SDK and AgentScope framework.
- JSON parsing uses `org.json:json` across all REST-based examples.
- Environment variables must be set before use: `GOOGLE_API_KEY`, `GEMINI_API_KEY`, `OPENAI_API_KEY`.
