package com.markwatson.agentscope;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.GeminiChatModel;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.agentscope.core.tool.Toolkit;

/**
 * Demonstrates AgentScope tool use with a stub "get weather" tool.
 *
 * <p>The agent will call {@code getWeather(city)} when it needs weather data,
 * which returns a hard-coded stub response.
 *
 * <pre>
 *   export GEMINI_API_KEY=your_key_here
 *   make run-tool
 * </pre>
 */
public class ToolUseExample {

    // ------------------------------------------------------------------ //
    //  Stub tool class – annotated with @Tool / @ToolParam so AgentScope  //
    //  can auto-generate the JSON schema the LLM uses for tool calling.   //
    // ------------------------------------------------------------------ //
    public static class WeatherService {

        @Tool(description = "Get the current weather for a specified city")
        public String getWeather(
                @ToolParam(name = "city", description = "The name of the city") String city) {
            // Stub: in a real app you would call a weather API here
            return String.format("%s weather: Sunny, 25°C", city);
        }
    }

    // ------------------------------------------------------------------ //
    //  Main                                                                //
    // ------------------------------------------------------------------ //
    public static void main(String[] args) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: GEMINI_API_KEY environment variable is not set.");
            System.exit(1);
        }

        // Build the Gemini chat model
        GeminiChatModel model = GeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-2.5-flash")
                .build();

        // Register the stub weather tool
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new WeatherService());

        // Build the ReActAgent with the toolkit attached
        ReActAgent agent = ReActAgent.builder()
                .name("WeatherAssistant")
                .sysPrompt("You are a helpful weather assistant. Use the getWeather tool to look up weather information.")
                .model(model)
                .toolkit(toolkit)
                .build();

        // Ask about the weather – the agent will invoke the tool automatically
        Msg response = agent.call(
                Msg.builder()
                        .textContent("What is the weather like in Tokyo and Paris?")
                        .build()
        ).block();

        System.out.println("Agent response:");
        System.out.println(response.getTextContent());
    }
}
