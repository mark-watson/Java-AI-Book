package com.markwatson.openai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

public class OpenAICompletions {

    // Reuse a single client instance (connection pool + thread pool efficiency)
    private static final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    public static void main(String[] args) {
        var prompt = "Translate the following English text to French: 'Hello, how are you?'";
        var completion = getCompletion(prompt);
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt) {
        System.out.println("prompt: " + prompt);

        var params = ChatCompletionCreateParams.builder()
                .addUserMessage(prompt)
                .model(ChatModel.GPT_5_MINI)
                .build();

        ChatCompletion chatCompletion = client.chat().completions().create(params);

        var content = chatCompletion.choices().get(0).message().content().orElse("");
        System.out.println(content);
        return content;
    }


    /***
     * Utilities for using the OpenAI API
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
