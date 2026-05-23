# Using Local LLMs Using Ollama in Java Applications

Using local Large Language Models (LLMs) with [Ollama](https://ollama.ai) offers a range of advantages and applications that significantly enhance the accessibility and functionality of these powerful AI tools in various settings. Ollama is like the Docker system, but for easily downloading, running, and managing LLMs on your local computer. Ollama was originally written to support Apple Silicon Macs, but now supports Intel Macs, Linux, and Windows.

{width: "80%"}
![Architecture diagram](images/ollama-architecture.png)

## Advantages of Using Local LLMs with Ollama

### Accessibility and Ease of Use

Ollama democratizes the use of sophisticated LLMs by making them accessible to users of all technical backgrounds. You don't need to be an AI expert to leverage the capabilities of LLMs when using Ollama. The platform's user-friendly interface and simple text-based interaction make it intuitive and straightforward for anyone to start using LLMs locally.

### Privacy and Data Security

Running LLMs locally on your system via Ollama ensures that your data does not leave your device, which is crucial for maintaining privacy and security, especially when handling sensitive information. This setup prevents data from being sent to third-party servers, thus safeguarding it from potential misuse or breaches.

### Cost-Effectiveness

Using Ollama to run LLMs locally eliminates the need for costly cloud computing resources. This can be particularly advantageous for users who require extensive use of LLMs, as it avoids the recurring costs associated with cloud services.

### Customization and Control

Local deployment of LLMs through Ollama allows users to have greater control over the models and the computational environment. This includes the ability to choose which models to run and to configure settings to optimize performance according to specific hardware capabilities.

## Applications of Local LLMs with Ollama


### Personalized AI Applications

For hobbyists and personal use, Ollama allows the exploration of LLMs' capabilities such as text generation, language translation, and more, all within the privacy of one's own computer. This can be particularly appealing for those interested in building personalized AI tools or learning more about AI without making significant investments.

### Development and Testing

Ollama is well-suited for developers who need to integrate LLMs into their applications but wish to do so in a controlled and cost-effective manner. It is particularly useful in development environments where frequent testing and iterations are required. The local setup allows for quick changes and testing without the need to interact with external servers.

### Educational and Research Purposes

Educators and researchers can benefit from the local deployment of LLMs using Ollama. It provides a platform for experimenting with AI models without the need for extensive infrastructure, making it easier to teach AI concepts and conduct research in environments with limited resources.

In summary, using local LLMs with Ollama not only makes powerful AI tools more accessible and easier to use but also ensures privacy, reduces costs, and provides users with greater control over their AI applications. Whether for professional development, research, or personal use, Ollama offers a versatile and user-friendly platform for exploring the potential of LLMs locally.

## Java Library to Use Ollama's REST API

The library defined in the directory **Java-AI-Book-Code/ollama-llm-client** defines a class named **OllamaLlmClient** with a method **getCompletion** that sends a JSON payload to a server and reads the response. Here's an explanation of what each significant part of the method does:

- Build JSON request payload: Constructs a JSON object `message` containing `prompt`, `model`, and `stream` (set to false to receive the full response at once instead of a stream).
- Prepare HTTP Request: Uses Java 11 `HttpRequest` builder to configure a `POST` request to `/api/generate` with the JSON payload as the body publisher and sets the content type header.
- Execute HTTP Request: Sends the request using a static shared `HttpClient` instance with a defined request timeout (defaulting to 3 minutes).
- Process Server Response: Parses the response body string as a `JSONObject` and extracts the value of the `response` key, which is returned.

In summary, this method sends a JSON payload containing a prompt and model name to a specified server endpoint, reads the JSON response from the server, extracts a specific field from the JSON response, and returns that field's value.


```java
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
```



## Example Using the Library

The Java library for getting local LLM text completions using Ollama contains a unit test that contains an example showing how to call the API:

```java
        String r = OllamaLlmClient.getCompletion(
                "Translate the following English text to French: 'Hello, how are you?'",
                "gemma3:1b");
        System.out.println("completion: " + r);
```

The output looks like:

```console

prompt: Translate the following English text to French: 'Hello, how are you?', modelName: mistral
completion:  In French, "Hello, how are you?" can be translated as "Bonjour, comment allez-vous?" or simply "Comment allez-vous?" depending on the context.
```

For reference the JSON response object from the API call looks like this:

```text
{"model":"mistral","created_at":"2024-05-05T19:38:26.893374Z","response":" In French, \"Hello, how are you?\" can be translated as \"Bonjour, comment allez-vous?\" or simply \"Comment allez-vous?\" depending on the context.","done":true,"context":[733,16289,28793, ...],"total_duration":1777944500,"load_duration":563601792,"prompt_eval_count":25,"prompt_eval_duration":133415000,"eval_count":41,"eval_duration":1079766000}
```


## Extraction of Facts and Relationships from Text Data

Traditional methods for extracting email addresses, names, addresses, etc. from text included the use of hand-crafted regular expressions and custom software. LLMs are text processing engines with knowledge of grammar, sentence structure, and some real world embedded knowledge. Using LLMs can reduce the development time of information extraction systems.

There are sample text prompts in the directory **Java-AI-Book-Code/prompts** and we will specifically use the file ** two-shot-2-var.txt** that is listed here:

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

The example code is a test method in **OllamaLlmClientTest. testTwoShotTemplate()** that is shown here:

```java
        String inputText = "Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";
        String prompt0 = OllamaLlmClient.readFileToString("../prompts/two-shot-2-var.txt");
        System.out.println("prompt0: " + prompt0);
        String prompt = OllamaLlmClient.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);
        String r = OllamaLlmClient.getCompletion(prompt, "gemma3:1b");
        System.out.println("two shot extraction completion: " + r);
```

The output is (edited for brevity):

```text
two shot extraction completion:
{
  "name": "Mark Johnson",
  "address": "102 Dunston Street, Berkeley, California",
  "email": "mjess@foobar.com"
}
```


## Using LLMs to Summarize Text

LLMs bring a new level of ability to text summarization tasks. With their ability to process massive amounts of information and "understand" natural language, they're able to capture the essence of lengthy documents and distill them into concise summaries.

Here is a listing or the prompt file:

```text
Summarize the following text: "{input_text}"
Output:
```

The example code is in the test **OllamaLlmClientTest. testSummarization()** listed here:

```java
        String inputText = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.";
        String prompt0 = OllamaLlmClient.readFileToString("../prompts/summarization_prompt.txt");
        System.out.println("prompt0: " + prompt0);
        String prompt = OllamaLlmClient.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);
        String r = OllamaLlmClient.getCompletion(prompt, "gemma3:1b");
        System.out.println("summarization completion: " + r);
```

The output is (edited for brevity):

```text
summarization completion:

Here is a summary of the text:

Jupiter is the 5th planet from the Sun and the largest gas giant in our Solar System, with a mass 1/1000 that of the Sun and 2.5 times that of all other planets combined. It's one of the brightest objects visible to the naked eye and has been known since ancient times. On average, it's the 3rd-brightest natural object in the night sky after the Moon and Venus.
```

