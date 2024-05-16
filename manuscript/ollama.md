# Using Local LLMs Using Ollama in Java Applications

*Note: May 15, 2024: this chapter is only 90% complete.*

Using local Large Language Models (LLMs) with [Ollama](https://ollama.ai) offers a range of advantages and applications that significantly enhance the accessibility and functionality of these powerful AI tools in various settings. Ollama is like the Docker system, but for easily downloading, running, and managing LLMs on your local computer. Ollama was originally written to support Apple Silicon Macs, but now supports Intel Macs, Linux, and Windows.

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

Te library defined in the directory ** Java-AI-Book-Code/ollama-llm-client** defines a class named **OllamaLlmClient** with a method **getCompletion** that sends a JSON payload to a server and reads the response. Here's an explanation of what each significant part of the method does:

- Create JSON Payload: It constructs a JSON object (message) containing three key-value pairs: prompt (a string provided by the caller), model (also a string provided by the caller indicating the model name), and stream (a boolean value set to false).
- Prepare HTTP Connection: It creates a URI object pointing to the server's URL (http://localhost:11434/api/generate), converts it to a URL object, and opens a connection to it. The connection is configured to send output and to use application/json as the content type.
- Send JSON Payload: It converts the JSON object to a byte array using UTF-8 encoding and sends it to the server through the connection's output stream.
- Read Server Response: It reads the server's response using a BufferedReader that wraps the connection's input stream, appending each line of the response to a StringBuilder object.
- Disconnect: It explicitly disconnects the HTTP connection.
- Process Server Response: It converts the response string back into a JSON object and extracts the value associated with the key response. This value is then returned by the method.

In summary, this method sends a JSON payload containing a prompt and model name to a specified server endpoint, reads the JSON response from the server, extracts a specific field from the JSON response, and returns that field's value.


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

public class OllamaLlmClient {

    public static void main(String[] args) throws Exception {
        String prompt = "Translate the following English text to French: 'Hello, how are you?'";
        String completion = getCompletion(prompt, "mistral");
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt, String modelName) throws Exception {
        System.out.println("prompt: " + prompt + ", modelName: " + modelName);
 
        // New JSON message format
        JSONObject message = new JSONObject();
        message.put("prompt", prompt);
        message.put("model", modelName);
        message.put("stream", false);
        URI uri = new URI("http://localhost:11434/api/generate");
        URL url = uri.toURL();
        //System.out.println("jsonBody: " + jsonBody);
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");
        // Send the JSON payload
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = message.toString().getBytes("utf-8");
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
        String s = jsonObject.getString("response");
        return s;
    }

}
```



## Example Using the Library

The Java library for getting local LLM text completions using Ollama contains a unit test that contains an example showing how to call the API:

```java
String r =
  OllamaLlmClient.getCompletion(
    "Translate the following English text to French: 'Hello, how are you?'",
    "mistral");
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
String input_text = "Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";
String prompt0 = OllamaLlmClient.readFileToString("../prompts/two-shot-2-var.txt");
System.out.println("prompt0: " + prompt0);
String prompt = OllamaLlmClient.promptVar(prompt0, "{input_text}", input_text);
System.out.println("prompt: " + prompt);
String r =
  OllamaLlmClient.getCompletion(prompt, "llama3:instruct");
System.out.println("two shot extraction completion:\n" + r);
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
String input_text = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter. When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows, and is on average the third-brightest natural object in the night sky after the Moon and Venus.";

String prompt0 = OllamaLlmClient.readFileToString("../prompts/summarization_prompt.txt");
System.out.println("prompt0: " + prompt0);
String prompt = OllamaLlmClient.promptVar(prompt0, "{input_text}", input_text);
System.out.println("prompt: " + prompt);
String r =
  OllamaLlmClient.getCompletion(prompt, "llama3:instruct");
System.out.println("summarization completion:\n" + r);
```

The output is (edited for brevity):

```text
summarization completion:

Here is a summary of the text:

Jupiter is the 5th planet from the Sun and the largest gas giant in our Solar System, with a mass 1/1000 that of the Sun and 2.5 times that of all other planets combined. It's one of the brightest objects visible to the naked eye and has been known since ancient times. On average, it's the 3rd-brightest natural object in the night sky after the Moon and Venus.
```

