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
