# Using the OpenAI Large Language Model APIs in Java

Large Language Models (LLMs) signify a significant leap forward in the progression of artificial intelligence, with a pronounced impact on the field of natural language processing (NLP), data transformation, translation, and serve as a source of real world knowledge in AI applications. They are trained on vast corpora of text data (literally most published books and most of the web), learning to predict subsequent words in a sequence, which imbues them with the ability to generate human-like text, comprehend the semantics of language, and perform a variety of language-related tasks. The architecture of these models, typically based on deep learning paradigms such as Transformer, empowers them to encapsulate intricate patterns and relationships within language. These models are trained utilizing substantial computational resources.

The utility of LLMs extends across a broad spectrum of applications including but not limited to text generation, translation, summarization, question answering, and sentiment analysis. Their ability to understand and process natural language makes them indispensable tools in modern AI-driven solutions. However, with great power comes great responsibility. The deployment of LLMs raises imperative considerations regarding ethics, bias, and the potential for misuse. Moreover, the black-box nature of these models presents challenges in interpretability and control, which are active areas of research in the quest to make LLMs more understandable and safe. The advent of LLMs has undeniably propelled the field of NLP to new heights, yet the journey towards fully responsible and transparent utilization of these powerful models is an ongoing endeavor. 

In the development of practical AI systems, LLMs like those provided by OpenAI, Anthropic, and Hugging Face have emerged as pivotal tools for numerous applications including natural language processing, generation, and understanding. These models, powered by deep learning architectures, encapsulate a wealth of knowledge and computational capabilities. As a Racket Scheme enthusiast embarking on the journey of intertwining the elegance of Racket with the power of these modern language models, you are opening a gateway to a realm of possibilities that we begin to explore here.


## Java Library to Use OpenAI's APIs

The library code defined in the directory ** Java-AI-Book-Code/openai-llm-client** is designed to interact with the OpenAI API to accept a prompt string and get a text completion. Here's a breakdown of what each part of the code does:

The **getCompletion** method performs the following steps:

- Initialization: This method takes a prompt as input and retrieves the OpenAI API key from the environment variables.
- JSON Object Creation: Constructs a JSON object to define the user's role and the content (the prompt). This is added to a JSON array, which is then included in another JSON object along with the model name (gpt-3.5-turbo).
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

public class OpenAICompletionsExample {

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

}
```



## Example Applications

There is a unit test provided with this library that shows how to call the completion API:

```java
String r =
  OpenAICompletionsExample.getCompletion("Translate the following English text to French: 'Hello, how are you?'");
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

