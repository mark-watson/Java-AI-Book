# AgentScope + Gemini Example

AgentScope is a framework for building LLM-powered applications and multi-agent systems on the JVM. Here we only look at two simple examples: a hello world example and a tool use example.

Please refer to [AgentScope](https://java.agentscope.io) for more information.
More docs: [https://java.agentscope.io/en/quickstart/installation.html](https://java.agentscope.io/en/quickstart/installation.html).

## Background

**AgentScope** is an open-source, agent-oriented Java framework developed by **Alibaba Group**.
It provides a clean programming model for building LLM-powered applications and multi-agent
systems on the JVM. AgentScope supports a wide range of language models — including Alibaba's
own **DashScope / Qwen** family as well as Google **Gemini**, OpenAI, and others — and ships
with built-in primitives for tool calling, long-term memory, RAG, and multi-agent collaboration.

The Java edition (`java.agentscope.io`) mirrors the Python AgentScope library and integrates
naturally with Spring Boot, Quarkus, and Micronaut. The central abstraction is the `ReActAgent`,
which implements a Reasoning + Acting loop: the agent reasons about a user message, optionally
invokes tools, and returns a final `Msg` response.

This example wires `ReActAgent` to Google's **Gemini 2.5 Flash** model using the official
`google-genai` Java SDK.

---

## Prerequisites

- Java 17+
- Maven 3.8+
- A valid `GEMINI_API_KEY` ([get one here](https://aistudio.google.com/app/apikey))

## Setup

```bash
export GEMINI_API_KEY=your_key_here
```

## Build & Run

```bash
# Build fat jar and run
make run

# Clean build artifacts
make clean
```

Or with Maven directly:

```bash
mvn package -q
java -jar target/agentscope-gemini-1.0.0-SNAPSHOT.jar
```

## Project Structure

```
.
├── Makefile
├── pom.xml
└── src/main/java/com/markwatson/agentscope/
    └── Main.java
```

## Key Dependencies

| Artifact | Purpose |
|---|---|
| `io.agentscope:agentscope:1.0.9` | AgentScope all-in-one (core, agents, messaging) |
| `com.google.genai:google-genai:1.0.0` | Google GenAI SDK (Gemini models) |

## How It Works

1. `GeminiChatModel` is built with the API key and model name (`gemini-2.5-flash`).
2. `ReActAgent` wraps the model with a system prompt.
3. A `Msg` with text content is sent to the agent via `.call()`.
4. The reactive response is collected with `.block()` and the text is printed.
