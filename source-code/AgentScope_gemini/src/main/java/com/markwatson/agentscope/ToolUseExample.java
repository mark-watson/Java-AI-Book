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
