# Using the Google Gemini Large Language Model APIs in Java

This example is very similar to OpenAI API examples in the previous chapter. We use the Google Developer's documentation as a reference: [https://ai.google.dev/gemini-api/docs](https://ai.google.dev/gemini-api/docs).

## Java Library to Use OpenAI's APIs

The library code defined in the directory **Java-AI-Book-Code/gemini-llm-client** is designed to interact with the Gemini API to accept a prompt string and get a text completion. Here's a breakdown of what each part of the code does:

### CLASS: com.markwatson.gemini.GeminiCompletions

Provides a set of static methods for interacting
with the Google Gemini API and for performing related
string and file utility operations.

#### FUNCTION: main(String[] args)

The main program entry point. Serves as a simple
demonstration of the getCompletion() function.

  * ACTION:
    - Calls getCompletion() with the hardcoded
      prompt "How much is 11 + 22?".
    - Prints the returned result to standard output.

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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GeminiCompletions {

    public static void main(String[] args) throws Exception {
        String prompt = "How much is 11 + 22?";
        String completion = getCompletion(prompt);
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt) throws Exception {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IOException("GOOGLE_API_KEY environment variable not set.");
        }
        String model = "gemini-2.5-flash";
        URI uri = new URI("https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey);
        URL url = uri.toURL();
        System.out.println("\n\nurl:\n\n" + url + "\n\n");

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");        connection.setDoOutput(true);        String jsonBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + prompt + "\"}]}]}";        try (OutputStream os = connection.getOutputStream()) {            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        connection.disconnect();
        JSONObject jsonObject = new JSONObject(response.toString());
        return jsonObject.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
    }


    /***
     * Utilities for using the Gemini API
     */

    // read the contents of a file path into a Java string
    public static String readFileToString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path)).replace("\"", "\\\"");
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

There is a unit test provided with this library that shows how to call the completion API:

```java
String r = GeminiCompletions.getCompletion("Translate the following English text to French: 'Hello, how are you?'");
        System.out.println("completion: " + r);
```

Sample output is:

```console
$ make
mvn test -q # run test in quiet mode
prompt: Translate the following English text to French: 'Hello, how are you?'
completion: Bonjour, comment vas-tu ?
```

## Wrap Up

The Java test code for the Gemini API also contains two more complex examples, similar to what was listed in the last chapter for OpenAI API support.