# AgentScope Agent Oriented Framework.

AgentScope is an agent oriented programming framework for building LLM powered applications that has components for ReAct reasoning, tool calling, memory management, and multi agent collaboration.

Here we only use a subset of the Java implementation of AgentScope. For reference this is the [home web page for Agentscope](https://agentscope.io).

{width: "80%"}
![Architecture diagram](images/agentscope-architecture.png)

## Using the Java Implementation of AgentScope: Hello World

For reference here is the [GitHub repository for Java AgentScope](https://github.com/agentscope-ai/agentscope-java) and [here is the documentation](https://java.agentscope.io/en/intro.html).

We start with the example in **Java-AI-Book/source-code/AgentScope_gemini/src/main/java/com/markwatson/agentscope/Main.java**. This code demonstrates the integration of the AgentScope framework with the Google Gemini language model to create a reasoning-based agent within a Java environment. By utilizing the `ReActAgent` class, the application implements a "Reasoning and Acting" loop, allowing the agent to process complex prompts by breaking them down into logical steps. The setup process begins with the secure retrieval of an API key from a system environment variable ensuring that sensitive credentials are not hard-coded into the source. Once validated, an instance of the `GeminiChatModel` class is instantiated with specific parameters, such as the `gemini-2.5-flash` model identifier, providing the underlying intelligence for the agent. This architectural approach highlights the simplicity of modern agentic frameworks using method call chaining, where a high level builder pattern is used to define the assistant's persona and system prompts, effectively bridging the gap between standard Java enterprise development and the burgeoning field of autonomous AI agents.


```java
package com.markwatson.agentscope;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.GeminiChatModel;

/**
 * AgentScope ReActAgent demo using Google Gemini (gemini-2.5-flash).
 *
 * <p>Set the environment variable GEMINI_API_KEY before running:
 * <pre>
 *   export GEMINI_API_KEY=your_key_here
 *   mvn package
 *   java -jar target/agentscope-gemini-1.0.0-SNAPSHOT.jar
 * </pre>
 */
public class Main {

    public static void main(String[] args) {
        // Build the Gemini chat model via shared config
        GeminiChatModel model = GeminiConfig.createModel();

        // Build the ReActAgent
        ReActAgent agent = ReActAgent.builder()
                .name("Assistant")
                .sysPrompt("You are a helpful AI assistant.")
                .model(model)
                .build();

        // Send a message and print the response
        Msg response = agent.call(
                Msg.builder()
                        .textContent("Hello! Tell me a fun fact about Java programming.")
                        .build()
        ).block();

        System.out.println("=== Agent Response ===");
        System.out.println(response.getTextContent());
    }
}
```

This simple example relies on the fluent API provided by the `AgentScope` core library, specifically the `GeminiChatModel.builder()` and `ReActAgent.builder()` methods. This pattern allows for a clean separation between the model's configuration, such as the API key and model version, and the agent's behavioral definition, including its name and system instructions. By encapsulating these details within the builder, the code remains readable and easily extensible for more complex agent behaviors or multi-model configurations.

Once the agent is initialized the communication is handled via the `Msg` object, which standardizes the format for inputs and outputs. The call to `agent.call().block()` demonstrates a synchronous execution flow where the application waits for the agent to complete its reasoning and generate a response before proceeding. This straightforward interaction demonstrates how quickly developers can transition from boilerplate setup to functional AI driven logic, extracting the text content from the final message object to provide immediate feedback to the user.

When we run this code, the output trace represents the initial "Hello World" execution of the AgentScope framework, providing a baseline for verifying the connectivity between the Java application and the Google Gemini backend. When the user executes the **make run** command, the JVM loads the `Main` class and initializes the `GeminiChatModel` using the provided API credentials. The console log reveals an immediate interaction with the Google `GenAI` client, including a warning about environment variable precedence that ensures developers are aware of which key is actively billing. The final "Agent response" section confirms that the ReActAgent successfully transmitted the text prompt to the `gemini-2.5-flash` model, received a structured response regarding the historical origins of the Java language, and printed it to the standard output. Here is sample output:

```
$ make run
java -cp target/agentscope-gemini-1.0.0-SNAPSHOT.jar com.markwatson.agentscope.Main
Mar 25, 2026 1:49:28 PM com.google.genai.ApiClient getApiKeyFromEnv
WARNING: Both GOOGLE_API_KEY and GEMINI_API_KEY are set. Using GOOGLE_API_KEY.
Agent response:
Hello there! Here's a fun fact about Java programming:

Java was originally named **"Oak"** by its creator, James Gosling, after an oak tree outside his office. However, it was later renamed **"Java"** after the type of coffee the developers were drinking a lot of during its creation – which is why the iconic Java logo is a steaming coffee cup! ☕
```

The trace demonstrates the efficiency of the gemini-2.5-flash model in a zero-shot conversational context. Despite the internal complexity of the ReAct (Reasoning and Acting) architecture, the agent identifies that the user's request for a "fun fact" does not require external tools, leading it to generate a direct response from its internal training data. This illustrates the "path of least resistance" in agentic workflows: the agent only engages in complex multi-step reasoning when the task demands it, otherwise acting as a highly responsive chat interface.

Technically, the log confirms that the `agent.call().block()` sequence in the source code functions as expected, holding the main thread until the remote inference is complete. The inclusion of the coffee related trivia and the steaming cup emoji highlights the model's ability to maintain a helpful, engaging persona as defined by the system prompt. For a developer, this successful run serves as the critical "smoke test," proving that the environment, dependencies, and network permissions are all correctly configured before moving on to more advanced tool integrated examples.

## Using the Java Implementation of AgentScope: Tool Use

Here is an example defining and using three tools that can be found in the file **Java-AI-Book/source-code/AgentScope_gemini/src/main/java/com/markwatson/agentscope/ToolUseExample.java**
 
In this section, we expand the capabilities of our agent by introducing the tool calling mechanism, enabling a Large Language Model to interact directly with external services and the local filesystem. The following code defines two service classes, `WeatherService` and `FileService`, which use the `@Tool` and `@ToolParam` annotations to provide the agent with semantic descriptions of available functions, specifically for retrieving weather data, listing directory contents, and reading file snippets. These tools are bundled into a `Toolkit` and attached to the `ReActAgent`, allowing the model to transition from simple text generation to an iterative "Reasoning and Acting" cycle. By processing complex, multi-step queries—such as searching for specific file types and summarizing their contents, and the agent demonstrates its ability to autonomously select the appropriate tool, pass the correct parameters, and synthesize the results into a coherent response.

```java
package com.markwatson.agentscope;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.GeminiChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates AgentScope tool use with a stub "get weather" tool plus two
 * real filesystem tools: {@code list_dir} and {@code read_file}.
 *
 * <p>The agent is asked two things:
 * <ol>
 *   <li>What is the weather in Tokyo and Paris?</li>
 *   <li>List the current directory and display the first 10 lines of every
 *       {@code .md} file found there.</li>
 * </ol>
 *
 * <pre>
 *   export GEMINI_API_KEY=your_key_here
 *   make run-tool
 * </pre>
 */
public class ToolUseExample {

    // ------------------------------------------------------------------ //
    //  Stub weather tool                                                   //
    // ------------------------------------------------------------------ //
    public static class WeatherService {

        @Tool(description = "Get the current weather for a specified city")
        public String getWeather(
                @ToolParam(name = "city", description = "The name of the city") String city) {
            // Stub: in a real app you would call a weather API here
            return "%s weather: Sunny, 25°C".formatted(city);
        }
    }

    // ------------------------------------------------------------------ //
    //  Filesystem tools                                                    //
    // ------------------------------------------------------------------ //
    public static class FileService {

        @Tool(description = "List the files and sub-directories inside a directory")
        public String listDir(
                @ToolParam(name = "path", description = "Absolute or relative path of the directory to list") String path) {
            try (Stream<Path> entries = Files.list(Path.of(path))) {
                String listing = entries
                        .map(p -> (Files.isDirectory(p) ? "[DIR]  " : "[FILE] ") + p.getFileName())
                        .sorted()
                        .collect(Collectors.joining("\n"));
                return listing.isEmpty() ? "(empty directory)" : listing;
            } catch (IOException e) {
                return "Error listing directory: " + e.getMessage();
            }
        }

        @Tool(description = "Read the first N lines of a text file")
        public String readFile(
                @ToolParam(name = "path",       description = "Path to the file to read") String path,
                @ToolParam(name = "max_lines",  description = "Maximum number of lines to return (default 10)") int maxLines) {
            try {
                List<String> lines = Files.readAllLines(Path.of(path));
                return lines.stream()
                        .limit(maxLines <= 0 ? 10 : maxLines)
                        .collect(Collectors.joining("\n"));
            } catch (IOException e) {
                return "Error reading file: " + e.getMessage();
            }
        }
    }

    // ------------------------------------------------------------------ //
    //  Main                                                                //
    // ------------------------------------------------------------------ //
    public static void main(String[] args) {
        // Build the Gemini chat model via shared config
        GeminiChatModel model = GeminiConfig.createModel();

        // Register all tools
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherService());
        toolkit.registerTool(new FileService());

        // Build the ReActAgent with the toolkit attached
        ReActAgent agent = ReActAgent.builder()
                .name("AssistantAgent")
                .sysPrompt("You are a helpful assistant with access to weather data and the local filesystem.")
                .model(model)
                .toolkit(toolkit)
                .build();

        // --- Query 1: weather ---
        Msg weatherResponse = agent.call(
                Msg.builder()
                        .textContent("What is the weather like in Tokyo and Paris?")
                        .build()
        ).block();

        System.out.println("=== Weather Query ===");
        System.out.println(weatherResponse.getTextContent());

        // --- Query 2: filesystem ---
        String cwd = System.getProperty("user.dir");
        Msg fsResponse = agent.call(
                Msg.builder()
                        .textContent(
                                "List the directory \"%s\" and for every .md file you find there, display its name followed by its first 10 lines."
                                        .formatted(cwd))
                        .build()
        ).block();

        System.out.println("\n=== Filesystem Query ===");
        System.out.println(fsResponse.getTextContent());
    }
}
```

The core of this example lies in the declarative registration of tools via the `Toolkit` class. By annotating standard Java methods, you provide the underlying Gemini model with documentation it can consult when it encounters a problem it cannot solve with internal knowledge alone. For instance, when tasked with listing a directory, the agent recognizes that its internal training data doesn't include your local files; it then triggers a call to `listDir`, processes the returned string, and intelligently decides to call `readFile` for each relevant `.md` file it identifies in the output.

This orchestration is managed by the `ReActAgent`, which handles the complex loop of generating a thought, executing a tool call, and observing the result. The use of `System.getProperty("user.dir")` ensures the agent has a valid starting point for its filesystem operations, while the `Msg` builder facilitates a clean exchange of information. This pattern effectively transforms the AI from a passive chatbot into an active participant capable of performing real-world tasks and handling I/O operations within a secure, developer-defined sandbox.

The following sample output trace illustrates the actual runtime behavior of the tool-augmented agent as it executes on the JVM. Upon launching the application via **make run-tool**, the `AgentScope` framework initializes the `Toolkit`, explicitly logging the registration of the `getWeather`, `listDir`, and `readFile` methods. The console output highlights the agent's ability to parse complex natural language queries and map them to these registered Java methods. In the first interaction, the agent correctly identifies the need for external data and invokes the weather stub for both Tokyo and Paris. In the second, more complex interaction, the agent performs a multi-step workflow: it first retrieves a directory listing, identifies a relevant Markdown file, and subsequently calls the file-reading utility to extract and display the header content of the project's documentation. Here is the sample output:

```
$ make run-tool
java -cp target/agentscope-gemini-1.0.0-SNAPSHOT.jar com.markwatson.agentscope.ToolUseExample
Mar 25, 2026 1:50:53 PM com.google.genai.ApiClient getApiKeyFromEnv
WARNING: Both GOOGLE_API_KEY and GEMINI_API_KEY are set. Using GOOGLE_API_KEY.

[main] INFO io.agentscope.core.tool.Toolkit - Registered tool 'getWeather' in group 'ungrouped'
[main] INFO io.agentscope.core.tool.Toolkit - Registered tool 'listDir' in group 'ungrouped'
[main] INFO io.agentscope.core.tool.Toolkit - Registered tool 'readFile' in group 'ungrouped'

=== Weather Query ===
The weather in Tokyo is Sunny with a temperature of 25°C. In Paris, it's also Sunny with a temperature of 25°C.

=== Filesystem Query ===
The file README.md has the following first 10 lines:
``
# AgentScope + Gemini Example

AgentScope is a framework for building LLM-powered applications and multi-agent systems on the JVM. Here we only look at two simple examples: a hello world example and a tool use example.

Please refer to [AgentScope](https://java.agentscope.io) for more information.
More docs: [https://java.agentscope.io/en/quickstart/installation.html](https://java.agentscope.io/en/quickstart/installation.html).

## Background

**AgentScope** is an open-source, agent-oriented Java framework developed by **Alibaba Group**.
``
```

## Wrap Up: From Simple Chat to Autonomous Tool Use

The transition from a basic conversational agent to a tool augmented system represents a significant leap in the utility of LLM powered applications. In the first example, we established the fundamental plumbing required to connect a Java application to the Google Gemini backend using the `AgentScope framework`. This "Hello World" implementation proved that a ReActAgent can function as a standard chat interface, processing natural language and maintaining a persona with minimal boilerplate code. It serves as the foundation, ensuring that environment variables, network configurations, and model authentication are correctly synchronized before introducing more complex logic.

The second example elevated the agent's role from a passive information retriever to an active participant in the local computing environment. By registering a `Toolkit` containing both a simulated weather service and a concrete filesystem service, we demonstrated how the agent utilizes its reasoning loop to solve multi step problems. The ability of the agent to "plan" its actions by first listing a directory, identifying relevant files, and then selectively reading their content, showcases the power of the Reasoning and Acting (ReAct) paradigm. This architecture allows developers to build safe, sandboxed environments where AI can perform real world tasks, transforming a static model into a dynamic assistant capable of interacting with proprietary data and external APIs.

Together, these examples illustrate the scalability of the `AgentScope framework`. Whether you are building a simple customer service bot or a complex autonomous researcher capable of navigating a codebase, the pattern remains consistent: define the model, describe the tools, and let the agent's reasoning engine bridge the gap. As you move forward, you can extend these concepts by adding more specialized tools, implementing custom error handling logic, or even orchestrating multiple agents to collaborate on shared tasks within the same JVM environment.