# Using the Google Gemini Large Language Model APIs in Java

This example is very similar to OpenAI API examples in the previous chapter. We use the Google Developer's documentation as a reference: [https://ai.google.dev/gemini-api/docs](https://ai.google.dev/gemini-api/docs). We implement methods for completions and completions also using Google’s search tool.

{width: "80%"}
![Architecture diagram](images/gemini-architecture.png)

## Java Library to Use Google Gemini's APIs

The library code defined in the directory **Java-AI-Book-Code/gemini-llm-client** is designed to interact with the Gemini API to accept a prompt string and get a text completion. Here's a breakdown of what each part of the code does:

### CLASS: com.markwatson.gemini.GeminiCompletions

Provides a set of static methods for interacting
with the Google Gemini API and for performing related
string and file utility operations.

#### FUNCTION: getCompletion(String prompt)

Sends a text prompt to the Gemini API and returns
the generated text completion.

  * INPUT:
    - A String 'prompt'.
  * REQUIRES:
    - The 'GOOGLE_API_KEY' environment variable
      must be set.
  * ACTION:
    - Constructs an HTTP POST request to the
      'gemini-2.5-flash' model endpoint.
    - Packages the prompt into the required
      JSON body.
  * ON SUCCESS:
    - Returns the 'text' field from the API's
      JSON response as a String.
  * ON FAILURE:
    - Throws an IOException if the API key is
      not found.


#### FUNCTION: getCompletionWithSearch(String prompt)

Sends a text prompt to the Gemini API using the search tool and returns the generated text completion.

  * INPUT:
    - A String 'prompt'.
  * REQUIRES:
    - The 'GOOGLE_API_KEY' environment variable
      must be set.
  * ACTION:
    - Constructs an HTTP POST request to the
      'gemini-2.5-flash' model endpoint.
    - Packages the prompt into the required
      JSON body.
  * ON SUCCESS:
    - Returns the 'text' field from the API's
      JSON response as a String.
  * ON FAILURE:
    - Throws an IOException if the API key is
      not found.

#### FUNCTION: readFileToString(String filePath)

Reads the entire contents of a specified file into
a single string.

  * INPUT:
    - A String 'filePath' pointing to a file.
  * ACTION:
    - Reads all bytes from the file.
    - Escapes all double-quote (") characters
      found in the content.
  * ON SUCCESS:
    - Returns the file's contents as a String.
  * ON FAILURE:
    - Throws an IOException if the file cannot
      be found or read.

#### FUNCTION: replaceSubstring(...)

A utility function that replaces all occurrences of a
substring within a string.

  * INPUT:
    - An original string.
    - The substring to replace.
    - The replacement string.
  * OUTPUT:
    - Returns the new string with replacements made.

#### FUNCTION: promptVar(...)

A simple templating utility for substituting a
placeholder in a string.

  * INPUT:
    - A template string.
    - A placeholder name to find.
    - The value to substitute.
  * OUTPUT:
    - Returns the new string with the placeholder
      replaced by the value.

#### Code Listing

```java
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
```

In the next section we write a unit test for this Java class to demonstrate text completion.

## Example Applications

Here are unit tests provided with this library that shows how to call each API:

```java
    @Test
    @DisplayName("Simple completion returns a non-empty response")
    void testCompletion() throws Exception {
        String r = GeminiCompletions
                .getCompletion("Translate the following English text to French: 'Hello, how are you?'");
        System.out.println("completion: " + r);
        assertNotNull(r, "Completion should not be null");
        assertFalse(r.isEmpty(), "Completion should not be empty");
    }

    @Test
    @DisplayName("Two-shot template prompt returns a non-empty response")
    void testTwoShotTemplate() throws Exception {
        String input_text = "Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";
        String prompt0 = GeminiCompletions.readFileToString("../prompts/two-shot-2-var.txt");
        System.out.println("prompt0: " + prompt0);
        String prompt = GeminiCompletions.promptVar(prompt0, "{input_text}", input_text);
        System.out.println("prompt: " + prompt);
        String r = GeminiCompletions.getCompletion(prompt);
        System.out.println("two shot extraction completion: " + r);
        assertNotNull(r, "Two-shot completion should not be null");
        assertFalse(r.isEmpty(), "Two-shot completion should not be empty");
    }

    @Test
    @DisplayName("Summarization prompt returns a non-empty response")
    void testSummarization() throws Exception {
        String input_text = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.";
        String prompt0 = GeminiCompletions.readFileToString("../prompts/summarization_prompt.txt");
        System.out.println("prompt0: " + prompt0);
        String prompt = GeminiCompletions.promptVar(prompt0, "{input_text}", input_text);
        System.out.println("prompt: " + prompt);
        String r = GeminiCompletions.getCompletion(prompt);
        System.out.println("summarization completion: " + r);
        assertNotNull(r, "Summarization result should not be null");
        assertFalse(r.isEmpty(), "Summarization result should not be empty");
    }

    @Test
    @DisplayName("Completion with Google Search grounding returns a non-empty response")
    void testCompletionWithSearch() throws Exception {
        String prompt = "What is the current stock price of Google?";
        String r = GeminiCompletions.getCompletionWithSearch(prompt);
        System.out.println("Search completion: " + r);
        assertNotNull(r, "Search completion should not be null");
        assertFalse(r.isEmpty(), "Search completion should not be empty");
    }
```

Sample output is:

```console
$ make
mvn test -q # run test in quiet mode
completion: Here are a few ways to say this in French, depending on the level of formality:

*   **Formal/Standard:** *Bonjour, comment allez-vous ?*
*   **Informal/Casual:** *Salut, ça va ?*
*   **Neutral:** *Bonjour, comment ça va ?*

Search completion: As of the market close on December 18, 2025, the stock prices for **Alphabet Inc. (Google)** are as follows:

*   **Alphabet Inc. Class A (GOOGL):** ~$302.01
*   **Alphabet Inc. Class C (GOOG):** ~$303.29


prompt0: Given the two examples below, extract the names, addresses, and email addresses of individuals mentioned later as Process Text. Format the extracted information in JSON, with keys for \"name\", \"address\", and \"email\". If any information is missing, use \"null\" for that field. Be very concise in your output by providing only the output JSON.

Example 1:
Text: \"John Doe lives at 1234 Maple Street, Springfield. His email is johndoe@example.com.\"
Output: 
{
  \"name\": \"John Doe\",
  \"address\": \"1234 Maple Street, Springfield\",
  \"email\": \"johndoe@example.com\"
}

Example 2:
Text: \"Jane Smith has recently moved to 5678 Oak Avenue, Anytown. She hasn't updated her email yet.\"
Output: 
{
  \"name\": \"Jane Smith\",
  \"address\": \"5678 Oak Avenue, Anytown\",
  \"email\": null
}

Process Text: \"{input_text}\"
Output:

prompt: Given the two examples below, extract the names, addresses, and email addresses of individuals mentioned later as Process Text. Format the extracted information in JSON, with keys for \"name\", \"address\", and \"email\". If any information is missing, use \"null\" for that field. Be very concise in your output by providing only the output JSON.

Example 1:
Text: \"John Doe lives at 1234 Maple Street, Springfield. His email is johndoe@example.com.\"
Output: 
{
  \"name\": \"John Doe\",
  \"address\": \"1234 Maple Street, Springfield\",
  \"email\": \"johndoe@example.com\"
}

Example 2:
Text: \"Jane Smith has recently moved to 5678 Oak Avenue, Anytown. She hasn't updated her email yet.\"
Output: 
{
  \"name\": \"Jane Smith\",
  \"address\": \"5678 Oak Avenue, Anytown\",
  \"email\": null
}

Process Text: \"Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.\"
Output:

two shot extraction completion: {
  "name": "Mark Johnson",
  "address": "102 Dunston Street, Berkeley California",
  "email": "mjess@foobar.com"
}
prompt0: Summarize the following text: \"{input_text}\"
Output:

prompt: Summarize the following text: \"Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.\"
Output:

summarization completion: Jupiter is the fifth planet from the Sun and the largest in the Solar System. A gas giant named after the Roman god, it is more massive than all other planets combined and stands as the third-brightest natural object in the night sky.
```

## Wrap Up

The Java test code for the Gemini API also contains two more complex examples, similar to what was listed in the last chapter for OpenAI API support.