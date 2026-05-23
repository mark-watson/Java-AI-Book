package com.markwatson.ollama;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.json.JSONObject;

public class OllamaLlmClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:11434";
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(3);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        String prompt = "Translate the following English text to French: 'Hello, how are you?'";
        String completion = getCompletion(prompt, "mistral");
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt, String modelName) throws IOException, InterruptedException {
        return getCompletion(prompt, modelName, DEFAULT_BASE_URL);
    }

    public static String getCompletion(String prompt, String modelName, String baseUrl)
            throws IOException, InterruptedException {
        System.out.println("prompt: " + prompt + ", modelName: " + modelName);

        // Build JSON request payload
        var message = new JSONObject();
        message.put("prompt", prompt);
        message.put("model", modelName);
        message.put("stream", false);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(message.toString()))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());

        var jsonObject = new JSONObject(response.body());
        return jsonObject.getString("response");
    }

    /***
     * Utilities for using the Ollama LLM APIs
     */

    // read the contents of a file path into a Java string
    public static String readFileToString(String filePath) throws IOException {
        return Files.readString(Path.of(filePath));
    }

    public static String replaceSubstring(String originalString, String substringToReplace, String replacementString) {
        return originalString.replace(substringToReplace, replacementString);
    }

    public static String promptVar(String prompt0, String varName, String varValue) {
        String prompt = replaceSubstring(prompt0, varName, varValue);
        return replaceSubstring(prompt, varName, varValue);
    }
}
