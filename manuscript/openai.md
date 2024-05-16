# Using the OpenAI Large Language Model APIs in Java

*Note: May 15, 2024: this chapter is about 90% complete*

Large Language Models (LLMs) signify a significant leap forward in the progression of artificial intelligence, with a pronounced impact on the field of natural language processing (NLP), data transformation, translation, and serve as a source of real world knowledge in AI applications. They are trained on vast corpora of text data (literally most published books and most of the web), learning to predict subsequent words in a sequence, which imbues them with the ability to generate human-like text, comprehend the semantics of language, and perform a variety of language-related tasks. The architecture of these models, typically based on deep learning paradigms such as Transformer, empowers them to encapsulate intricate patterns and relationships within language. These models are trained utilizing substantial computational resources.

The utility of LLMs extends across a broad spectrum of applications including but not limited to text generation, translation, summarization, question answering, and sentiment analysis. Their ability to understand and process natural language makes them indispensable tools in modern AI-driven solutions. However, with great power comes great responsibility. The deployment of LLMs raises imperative considerations regarding ethics, bias, and the potential for misuse. Moreover, the black-box nature of these models presents challenges in interpretability and control, which are active areas of research in the quest to make LLMs more understandable and safe. The advent of LLMs has undeniably propelled the field of NLP to new heights, yet the journey towards fully responsible and transparent utilization of these powerful models is an ongoing endeavor. 

In the development of practical AI systems, LLMs like those provided by OpenAI, Anthropic, and Hugging Face have emerged as pivotal tools for numerous applications including natural language processing, generation, and understanding. These models, powered by deep learning architectures, encapsulate a wealth of knowledge and computational capabilities. Here we look at the basics for getting you, dear reader, started using the OpenAI APIs for text completion tasks in Java code. In the next chapter we do the same ex pet we will run local LLMs on our laptops using the [Ollama](https://ollama.ai) platform.

## Java Library to Use OpenAI's APIs

The library code defined in the directory **Java-AI-Book-Code/openai-llm-client** is designed to interact with the OpenAI API to accept a prompt string and get a text completion. Here's a breakdown of what each part of the code does:

The **getCompletion** method performs the following steps:

- Initialization: This method takes a prompt as input and retrieves the OpenAI API key from the environment variables.
- JSON Object Creation: Constructs a JSON object to define the user's role and the content (the prompt). This is added to a JSON array, which is then included in another JSON object along with the model name (here we are using gpt-3.5-turbo but you can also try gpt-4 that is more expensive but more capable.).
- API Request Setup: Constructs a URI for the OpenAI API endpoint and sets up a URL connection. It configures the connection to send data (output) and sets the request headers for content type (JSON) and authorization (using the retrieved API key).
- Sending the Request: Converts the JSON object to bytes and sends it through the connection's output stream.
- Response Handling: Reads the response from the API using a BufferedReader. The response is built into a string using a StringBuilder.
- Parsing the Response: Converts the response string back into a JSON object, extracts the relevant part of the JSON that contains the API's completion result (the translated text), and returns this result.
- Error Handling and Cleanup: The connection is explicitly disconnected after the response is read.


```java
package com.markwatson.openai;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAICompletions {

    public static void main(String[] args) throws Exception {
        String prompt = "Translate the following English text to French: 'Hello, how are you?'";
        String completion = getCompletion(prompt);
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt) throws Exception {
        System.out.println("prompt: " + prompt);
        String apiKey = System.getenv("OPENAI_API_KEY");
        String model = "gpt-3.5-turbo"; // Replace with the desired model

        // New JSON message format
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(message);
        //System.out.println("messages: " + messages.toString());
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("messages", messages);
        jsonBody.put("model", model);
        URI uri = new URI("https://api.openai.com/v1/chat/completions");
        URL url = uri.toURL();
        //System.out.println("jsonBody: " + jsonBody);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        // Send the JSON payload
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes("utf-8");
             os.write(input, 0, input.length);
        }

        StringBuilder response;
        // Read the response from the server
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println(response.toString());
        }

        ((HttpURLConnection) connection).disconnect();
        JSONObject jsonObject = new JSONObject(response.toString());
        JSONArray choices = jsonObject.getJSONArray("choices");
        JSONObject messageObject = choices.getJSONObject(0).getJSONObject("message");
        String content = messageObject.getString("content");
        //System.out.println("content: " + content);
        return content;
    }

    /***
     * Utilities for using the OpenAI API
     */

    // read the contents of a file path into a Java string
    public static String readFileToString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
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
String r =
  OpenAICompletions.getCompletion("Translate the following English text to French: 'Hello, how are you?'");
System.out.println("completion: " + r);
```

Sample output is:

```console
$ make
mvn test -q # run test in quiet mode
prompt: Translate the following English text to French: 'Hello, how are you?'
completion: Bonjour, comment vas-tu ?
```

For reference, the JSON response object returned from the OpenAI completion API looks like this:

```text
{"id": "chatcmpl-7LbgN6PJxHAfycOuHmGkw8nbpQMm1","object": "chat.completion","created": 1714936767,"model": "gpt-3.5-turbo-0125","choices": [{"index": 0,"message": {"role": "assistant","content": "Bonjour, comment vas-tu ?"},"logprobs": null,"finish_reason": "stop"}],"usage": {"prompt_tokens": 22,"completion_tokens": 7,"total_tokens": 29},"system_fingerprint": "fp_b410720239"}
```

## Extraction of Facts and Relationships from Text Data

Traditional methods for extracting email addresses, names, addresses, etc. from text included the use of hand-crafted regular expressions and custom software. LLMs are text processing engines with knowledge of grammar, sentence structure, and some real world embedded knowledge. Using LLMs can reduce the development time of information extraction systems.

The template we will use if in the file **Java-AI-Book-Code /prompts/two-shot-2-var.txt**:

```text
Given the two examples below, extract the names, addresses, and email addresses of individuals mentioned later as Process Text. Format the extracted information in JSON, with keys for "name", "address", and "email". If any information is missing, use "null" for that field. Be very concise in your output by providing only the output JSON.

Example 1:
Text: "John Doe lives at 1234 Maple Street, Springfield. His email is johndoe@example.com."
Output: 
{
  "name": "John Doe",
  "address": "1234 Maple Street, Springfield",
  "email": "johndoe@example.com"
}

Example 2:
Text: "Jane Smith has recently moved to 5678 Oak Avenue, Anytown. She hasn't updated her email yet."
Output: 
{
  "name": "Jane Smith",
  "address": "5678 Oak Avenue, Anytown",
  "email": null
}

Process Text: "{input_text}"
Output:
```


The example code for this section is in a Java Unit Test method **testTwoShotTemplate()**:

```java
String input_text = "Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";

String prompt0 = OpenAICompletions.readFileToString("../prompts/two-shot-2-var.txt");
System.out.println("prompt0: " + prompt0);
String prompt = OpenAICompletions.promptVar(prompt0, "{input_text}", input_text);
System.out.println("prompt: " + prompt);
String r =
OpenAICompletions.getCompletion(prompt);
System.out.println("two shot extraction completion:\n" + r);
```

The output looks like:

```console
two shot extraction completion:
{
  "name": "Mark Johnson",
  "address": "102 Dunston Street, Berkeley California",
  "email": "mjess@foobar.com"
}
```


## Using LLMs to Summarize Text

LLMs bring a new level of ability to text summarization tasks. With their ability to process massive amounts of information and "understand" natural language, they're able to capture the essence of lengthy documents and distill them into concise summaries. Two main types of summarization dominate with LLMs: extractive and abstractive. Extractive summarization pinpoints the most important sentences within the original text, while abstractive summarization  requires the LLM to paraphrase or generate new text to represent the core ideas. If you are interested in extractive summarization there is a chapter on this topic in my [Common Lisp AI book](https://leanpub.com/lovinglisp/read) (link to read online).

Here we use the prompt template file **Java-AI-Book-Code/prompts/summarization_prompt.txt**:

```text
Summarize the following text: "{input_text}"
Output:
```

The example code is in the Java Unit Test **testSummarization()**:

```java
String input_text = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.";
        String prompt0 = OpenAICompletions.readFileToString("../prompts/summarization_prompt.txt");
        System.out.println("prompt0: " + prompt0);
        String prompt = OpenAICompletions.promptVar(prompt0, "{input_text}", input_text);
        System.out.println("prompt: " + prompt);
        String r =
        OpenAICompletions.getCompletion(prompt);
        System.out.println("summarization completion:\n\n" + r);
```

The output is:

```console
summarization completion:

Jupiter is the largest planet in the Solar System and the fifth from the Sun. It is a gas giant with a mass one-thousandth that of the Sun. It is visible to the naked eye and has been known since ancient times. Jupiter is named after the Roman god Jupiter and is the third-brightest natural object in the night sky.
```
