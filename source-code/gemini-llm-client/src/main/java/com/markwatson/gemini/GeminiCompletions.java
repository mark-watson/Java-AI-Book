package com.markwatson.gemini;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class GeminiCompletions {

    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    public static String model = DEFAULT_MODEL;

    public static void main(String[] args) throws Exception {
        String prompt = "How much is 11 + 22?";
        String completion = getCompletion(prompt);
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt) throws Exception {
        var jsonBody = buildRequestBody(prompt);
        return executeRequest(jsonBody);
    }

    public static String getCompletionWithSearch(String prompt) throws Exception {
        var textPart = new JSONObject().put("text", prompt);
        var parts = new JSONArray().put(textPart);
        var content = new JSONObject().put("parts", parts);
        var contents = new JSONArray().put(content);

        var googleSearchTool = new JSONObject().put("google_search", new JSONObject());
        var tools = new JSONArray().put(googleSearchTool);

        var body = new JSONObject()
                .put("contents", contents)
                .put("tools", tools);

        return executeRequest(body.toString());
    }

    private static String buildRequestBody(String prompt) {
        var textPart = new JSONObject().put("text", prompt);
        var parts = new JSONArray().put(textPart);
        var content = new JSONObject().put("parts", parts);
        var contents = new JSONArray().put(content);
        return new JSONObject().put("contents", contents).toString();
    }

    private static String executeRequest(String jsonBody) throws Exception {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("GOOGLE_API_KEY environment variable not set.");
        }

        var uri = URI.create(
                "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey);

        var request = HttpRequest.newBuilder()
                .uri(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response;
        try (var client = HttpClient.newHttpClient()) {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {
            throw new IOException("Gemini API request failed (HTTP %d): %s"
                    .formatted(response.statusCode(), response.body()));
        }

        var jsonObject = new JSONObject(response.body());
        return jsonObject.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts")
                .getJSONObject(0).getString("text");
    }

    /***
     * Utilities for using the Gemini API
     */

    // read the contents of a file path into a Java string
    public static String readFileToString(String filePath) throws IOException {
        return Files.readString(Path.of(filePath)).replace("\"", "\\\"");
    }

    public static String replaceSubstring(String originalString, String substringToReplace, String replacementString) {
        return originalString.replace(substringToReplace, replacementString);
    }

    public static String promptVar(String prompt0, String varName, String varValue) {
        String prompt = replaceSubstring(prompt0, varName, varValue);
        return replaceSubstring(prompt, varName, varValue);
    }
}
