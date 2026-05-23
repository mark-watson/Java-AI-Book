package com.markwatson.langchain4j_ollama;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class OllamaLlmLangChain4j {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(120);

    public static void main(String[] args) {
        String prompt = "Translate the following English text to French: 'Hello, how are you?'";
        try {
            String completion = getCompletion(prompt, "mistral");
            System.out.println("completion: " + completion);
        } catch (Exception e) {
            System.err.println("Error getting completion: " + e.getMessage());
        }
    }

    public static String getCompletion(String prompt, String modelName) {
        System.out.println("\n\n**********\n\nprompt: " + prompt + ", modelName: " + modelName);

        String baseUrl = System.getenv("OLLAMA_BASE_URL");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(0.7)
                .timeout(DEFAULT_TIMEOUT)
                .build();

        String answer = model.chat(prompt);

        System.out.println(answer);
        return answer;
    }

    /***
     * Utilities for using the Ollama LLM APIs
     */

    // read the contents of a file path into a Java string
    public static String readFileToString(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }

    public static String promptVar(String prompt, String varName, String varValue) {
        return prompt.replace(varName, varValue);
    }
}
