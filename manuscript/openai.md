# Using the OpenAI Large Language Model APIs in Java

Large Language Models (LLMs) signify a significant leap forward in the progression of artificial intelligence, with a pronounced impact on the field of natural language processing (NLP), data transformation, translation, and serve as a source of real world knowledge in AI applications. They are trained on vast corpora of text data (literally most published books and most of the web), learning to predict subsequent words in a sequence, which imbues them with the ability to generate human-like text, comprehend the semantics of language, and perform a variety of language-related tasks. The architecture of these models, typically based on deep learning paradigms such as Transformer, empowers them to encapsulate intricate patterns and relationships within language. These models are trained utilizing substantial computational resources.

The utility of LLMs extends across a broad spectrum of applications including but not limited to text generation, translation, summarization, question answering, and sentiment analysis. Their ability to understand and process natural language makes them indispensable tools in modern AI-driven solutions. However, with great power comes great responsibility. The deployment of LLMs raises imperative considerations regarding ethics, bias, and the potential for misuse. Moreover, the black-box nature of these models presents challenges in interpretability and control, which are active areas of research in the quest to make LLMs more understandable and safe. The advent of LLMs has undeniably propelled the field of NLP to new heights, yet the journey towards fully responsible and transparent utilization of these powerful models is an ongoing endeavor. 

In the development of practical AI systems, LLMs like those provided by OpenAI, Anthropic, and Hugging Face have emerged as pivotal tools for numerous applications including natural language processing, generation, and understanding. These models, powered by deep learning architectures, encapsulate a wealth of knowledge and computational capabilities. Here we look at the basics for getting you, dear reader, started using the OpenAI APIs for text completion tasks in Java code. In the next chapter we do the same ex pet we will run local LLMs on our laptops using the [Ollama](https://ollama.ai) platform.

{width: "80%"}
![Architecture diagram](images/openai-architecture.png)

## Java Library to Use OpenAI's APIs

The library code defined in the directory **Java-AI-Book-Code/openai-llm-client** is designed to interact with the OpenAI API to accept a prompt string and get a text completion. Here's a breakdown of what each part of the code does:

The **getCompletion** method performs the following steps:

- Client Initialization: We reuse a single client instance of `OpenAIClient`, initialized via `OpenAIOkHttpClient.fromEnv()`, which automatically pulls the API key from the standard environment variables.
- Request Parameters Construction: Builds a `ChatCompletionCreateParams` object using its builder. We add the user message (the prompt) and set the model to `ChatModel.GPT_5_MINI`.
- Sending the Request: Calls `client.chat().completions().create(params)` which synchronously sends the request via OKHttp and receives the chat completion response.
- Parsing the Response: Extracts the first choice's message content using `chatCompletion.choices().get(0).message().content().orElse("")` and returns it.


```java
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
```

In the next section we write a unit test for this Java class to demonstrate text completion.

## Example Applications

There is a unit test provided with this library that shows how to call the completion API:

```java
        var result = OpenAICompletions.getCompletion(
                "Translate the following English text to French: 'Hello, how are you?'");
        System.out.println("completion: " + result);
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
        var inputText = "Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";
        var prompt0 = OpenAICompletions.readFileToString("../prompts/two-shot-2-var.txt");
        System.out.println("prompt0: " + prompt0);
        var prompt = OpenAICompletions.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);
        var result = OpenAICompletions.getCompletion(prompt);
        System.out.println("two shot extraction completion: " + result);
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
        var inputText = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.";
        var prompt0 = OpenAICompletions.readFileToString("../prompts/summarization_prompt.txt");
        System.out.println("prompt0: " + prompt0);
        var prompt = OpenAICompletions.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);
        var result = OpenAICompletions.getCompletion(prompt);
        System.out.println("summarization completion: " + result);
```

The output is:

```console
summarization completion:

Jupiter is the largest planet in the Solar System and the fifth from the Sun. It is a gas giant with a mass one-thousandth that of the Sun. It is visible to the naked eye and has been known since ancient times. Jupiter is named after the Roman god Jupiter and is the third-brightest natural object in the night sky.
```
