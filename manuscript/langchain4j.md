# LangChain for Java as an Abstraction for Different Large Language Models

LangChain4j aims to streamline the integration of AI and large language model (LLM) capabilities into Java applications by providing a unified API. This API supports various LLM providers, such as OpenAI, Mistral, and Google Vertex AI, and embedding stores like Pinecone and Vespa, eliminating the need to learn and implement specific APIs for each provider. We wrote Java code to interact with OpenAI APIs and local LLMs running on Ollama in the last two chapters. LangChain4j  provides abstract interfaces for many more models. This flexibility allows developers to switch between different LLMs or embedding stores without rewriting their code. LangChain4j currently supports over 10 popular LLM providers and more than 15 embedding stores, functioning similarly to Hibernate but for LLMs and embedding stores.

The framework also offers a comprehensive toolbox that encapsulates the community's collective experience in building LLM-powered applications over the past year. This toolbox includes tools for low-level prompt templating, memory management, and output parsing, as well as high-level patterns like Agents and Retrieval-Augmented Generation (RAGs). LangChain4j provides interfaces and multiple ready-to-use implementations for each pattern and abstraction, based on proven techniques. This makes it suitable for a wide range of applications, from chatbots to complete RAG pipelines, offering developers a variety of options to build sophisticated LLM-powered solutions efficiently.

{width: "80%"}
![Architecture diagram](images/langchain4j-architecture.png)

## Why Use an Abstraction Layer?

In the previous two chapters we wrote raw HTTP client code to interact with the OpenAI and Ollama APIs. While this approach teaches us how LLM APIs work at the wire level, it has practical disadvantages. Each provider has its own JSON schema for requests and responses, its own authentication mechanism, and its own model naming conventions. If your project needs to support multiple providers, or if you want the freedom to switch providers when pricing or capabilities change, you face the burden of maintaining multiple low-level HTTP integrations.

LangChain4j solves this problem by defining a single Java interface, `ChatLanguageModel`, that every provider implements. Your application code calls `model.generate(prompt)` regardless of whether the underlying model is OpenAI's GPT, a local Ollama instance, or Google's Gemini. Switching providers is a configuration change, not a code rewrite.

For reference the LangChain4j project documentation is available at [https://docs.langchain4j.dev](https://docs.langchain4j.dev).


## Maven Project Setup

The example project is in the directory **source-code/langchain4j-ollama**. The Maven POM file declares three dependencies: the LangChain4j core library, the OpenAI-compatible provider module (which also supports Ollama's API since Ollama exposes an OpenAI-compatible endpoint), and the `org.json` library for JSON manipulation in our utility methods.

{lang="xml",linenos=off}
~~~~~~~~
<dependencies>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j</artifactId>
        <version>0.30.0</version>
    </dependency>
    <dependency>
        <groupId>dev.langchain4j</groupId>
        <artifactId>langchain4j-open-ai</artifactId>
        <version>0.30.0</version>
    </dependency>
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20240303</version>
    </dependency>
</dependencies>
~~~~~~~~

The key insight here is the `langchain4j-open-ai` artifact. LangChain4j organizes provider support into separate modules so that your application only pulls in the dependencies for the providers you actually use. The OpenAI module works with any API that implements the OpenAI chat completions protocol, which includes Ollama when started with `ollama serve`.


## Implementation: The OllamaLlmLangChain4j Class

The main class provides a `getCompletion` method that wraps LangChain4j's `ChatLanguageModel` interface, along with several utility methods for file I/O and prompt template variable substitution. Let us walk through the complete implementation:

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.langchain4j_ollama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;

public class OllamaLlmLangChain4j {

    public static void main(String[] args) throws Exception {
        String prompt =
          "Translate the following English text to French:"
          + " 'Hello, how are you?'";
        String completion = getCompletion(prompt, "mistral");
        System.out.println("completion: " + completion);
    }

    public static String getCompletion(String prompt,
                         String modelName) throws Exception {
        System.out.println("\n\n**********\n\nprompt: "
                           + prompt
                           + ", modelName: " + modelName);
        String api_key = System.getenv("OPENAI_API_KEY");
        ChatLanguageModel model =
            OpenAiChatModel.withApiKey(api_key);
        String answer = model.generate(prompt);
        System.out.println(answer);
        return answer;
    }

    /***
     * Utilities for prompt template processing
     */

    public static String readFileToString(String filePath)
                                          throws IOException {
        Path path = Paths.get(filePath);
        return new String(Files.readAllBytes(path));
    }

    public static String replaceSubstring(
          String originalString,
          String substringToReplace,
          String replacementString) {
        return originalString.replace(
            substringToReplace, replacementString);
    }

    public static String promptVar(String prompt0,
                                   String varName,
                                   String varValue) {
        String prompt =
            replaceSubstring(prompt0, varName, varValue);
        return replaceSubstring(prompt, varName, varValue);
    }
}
~~~~~~~~

### Understanding the Core API Call

The heart of this class is the `getCompletion` method on lines 24 through 34. Notice how concise the LLM interaction is:

1. We retrieve the API key from the environment variable `OPENAI_API_KEY`.
2. We construct a `ChatLanguageModel` instance using `OpenAiChatModel.withApiKey(api_key)`. This single line replaces dozens of lines of HTTP connection setup, JSON serialization, and response parsing that we wrote by hand in the OpenAI chapter.
3. We call `model.generate(prompt)` which returns a plain Java `String` containing the model's response.

Compare this with the raw HTTP approach from the OpenAI chapter where we had to manually construct JSON request bodies, set HTTP headers, read input streams, and parse JSON responses. The LangChain4j abstraction reduces all of that ceremony to two lines of code.

### Prompt Template Utilities

The utility methods `readFileToString`, `replaceSubstring`, and `promptVar` implement a simple but effective prompt templating system. The `promptVar` method replaces placeholder variables like `{input_text}` in a prompt template string with actual values. This pattern is useful when you maintain a library of reusable prompt templates as text files, which is exactly what we do in the **source-code/prompts** directory.


## Prompt Templates: Two-Shot Entity Extraction

One of the most effective techniques for getting consistent structured output from an LLM is few-shot prompting, where you provide the model with examples of the desired input-output format before presenting the actual task. Our two-shot extraction prompt template, stored in the file **prompts/two-shot-2-var.txt**, demonstrates this:

{linenos=off}
~~~~~~~~
Given the two examples below, extract the names,
addresses, and email addresses of individuals mentioned
later as Process Text. Format the extracted information
in JSON, with keys for "name", "address", and "email".
If any information is missing, use "null" for that field.
Be very concise in your output by providing only the
output JSON.

Example 1:
Text: "John Doe lives at 1234 Maple Street, Springfield.
His email is johndoe@example.com."
Output: 
{
  "name": "John Doe",
  "address": "1234 Maple Street, Springfield",
  "email": "johndoe@example.com"
}

Example 2:
Text: "Jane Smith has recently moved to 5678 Oak Avenue,
Anytown. She hasn't updated her email yet."
Output: 
{
  "name": "Jane Smith",
  "address": "5678 Oak Avenue, Anytown",
  "email": null
}

Process Text: "{input_text}"
Output:
~~~~~~~~

The prompt begins with a clear task description specifying the desired output format (JSON with specific keys). Two worked examples follow, including one where a field is intentionally missing to show the model how to handle incomplete data. The placeholder `{input_text}` at the bottom is replaced at runtime by our `promptVar` utility method.

We also use a summarization prompt template stored in **prompts/summarization_prompt.txt**:

{linenos=off}
~~~~~~~~
Summarize the following text: "{input_text}"
Output:
~~~~~~~~

This is deliberately minimal. For summarization tasks an LLM typically needs very little instructional scaffolding beyond a clear directive.


## Test Examples

The JUnit test class exercises three distinct use cases: translation, structured entity extraction using two-shot prompting, and summarization. Each test loads a prompt template from disk, fills in variables, and sends the completed prompt through the LangChain4j abstraction layer.

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.langchain4j_ollama;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OllamaLlmLangChain4jTest extends TestCase {

    public OllamaLlmLangChain4jTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(
            OllamaLlmLangChain4jTest.class);
    }

    public void testCompletion() throws Exception {
        String r =
            OllamaLlmLangChain4j.getCompletion(
                "Translate the following English text"
                + " to French: 'Hello, how are you?'",
                "llama3.2:latest");
        System.out.println(
            "\n\n&&&&&&&&&&\n\ncompletion: " + r);
        assertTrue(true);
    }

    public void testTwoShotTemplate() throws Exception {
        String input_text =
            "Mark Smith enjoys living in Berkeley"
            + " California at 102 Dunston Street"
            + " and use mjess@foobar.com for"
            + " contacting him.";
        String prompt0 =
            OllamaLlmLangChain4j.readFileToString(
                "../prompts/two-shot-2-var.txt");
        String prompt =
            OllamaLlmLangChain4j.promptVar(
                prompt0, "{input_text}", input_text);
        String r =
            OllamaLlmLangChain4j.getCompletion(
                prompt, "llama3:instruct");
        System.out.println(
            "two shot extraction completion: " + r);
        assertTrue(true);
    }

    public void testSummarization() throws Exception {
        String input_text =
            "Jupiter is the fifth planet from the Sun"
            + " and the largest in the Solar System."
            + " It is a gas giant with a mass"
            + " one-thousandth that of the Sun, but"
            + " two-and-a-half times that of all the"
            + " other planets in the Solar System"
            + " combined. Jupiter is one of the"
            + " brightest objects visible to the naked"
            + " eye in the night sky, and has been"
            + " known to ancient civilizations since"
            + " before recorded history. It is named"
            + " after the Roman god Jupiter.";
        String prompt0 =
            OllamaLlmLangChain4j.readFileToString(
                "../prompts/summarization_prompt.txt");
        String prompt =
            OllamaLlmLangChain4j.promptVar(
                prompt0, "{input_text}", input_text);
        String r =
            OllamaLlmLangChain4j.getCompletion(
                prompt, "llama3:instruct");
        System.out.println(
            "summarization completion: " + r);
        assertTrue(true);
    }
}
~~~~~~~~

### Test 1: Translation (testCompletion)

The simplest test sends a direct English-to-French translation request. This exercises the basic round-trip through the LangChain4j API without any prompt template processing.

### Test 2: Two-Shot Entity Extraction (testTwoShotTemplate)

This test loads the two-shot prompt template from disk and substitutes the `{input_text}` variable with a test sentence containing a person's name, address, and email. The model is expected to return a JSON object with the extracted fields.

### Test 3: Summarization (testSummarization)

This test loads the summarization prompt template and substitutes a paragraph about the planet Jupiter. The model is expected to return a concise summary of the input text.


## Running the Examples

The project includes a **Makefile** that runs all three tests:

{lang="makefile",linenos=off}
~~~~~~~~
run:
	mvn test -q # run test in quiet mode
~~~~~~~~

Before running, ensure that you have Ollama running locally (`ollama serve`) and that you have pulled the required models:

{linenos=off}
~~~~~~~~
ollama pull llama3.2
ollama pull llama3
~~~~~~~~

You also need the `OPENAI_API_KEY` environment variable set since the LangChain4j OpenAI module requires it for authentication (Ollama accepts any non-empty key when accessed through the OpenAI-compatible endpoint).

The example program output is a few hundred lines due to the verbose prompt logging. Here is a small representative portion of the output showing the results of each test:

{linenos=off}
~~~~~~~~
$ make run

**********

prompt: Translate the following English text to French:
  'Hello, how are you?', modelName: llama3.2:latest
Bonjour, comment allez-vous ?

&&&&&&&&&&

completion: Bonjour, comment allez-vous ?

**********

prompt: Given the two examples below, extract the
  names, addresses, and email addresses ...
  Process Text: "Mark Smith enjoys living in Berkeley
  California at 102 Dunston Street and use
  mjess@foobar.com for contacting him."
  Output:
{
  "name": "Mark Smith",
  "address": "102 Dunston Street, Berkeley, California",
  "email": "mjess@foobar.com"
}

two shot extraction completion: {
  "name": "Mark Smith",
  "address": "102 Dunston Street, Berkeley, California",
  "email": "mjess@foobar.com"
}

**********

prompt: Summarize the following text:
  "Jupiter is the fifth planet from the Sun..."
  Output:
Jupiter is the largest planet in our Solar System
and the fifth from the Sun. A gas giant with a mass
two-and-a-half times that of all other planets
combined, it has been observed since ancient times
and is named after the Roman god Jupiter.

summarization completion: Jupiter is the largest
planet in our Solar System ...
~~~~~~~~


## Wrap Up

The key takeaway from this chapter is the value of abstraction in AI application development. By using LangChain4j's `ChatLanguageModel` interface we wrote a complete LLM client, including prompt template processing and three different NLP tasks, in under 70 lines of library code. The same application code works with OpenAI's cloud API, a local Ollama instance, or any other provider that LangChain4j supports, simply by changing the model construction line.

The prompt template utilities we built here, while simple, demonstrate a pattern that scales well to production systems. Maintaining prompts as external text files with variable placeholders separates prompt engineering concerns from application logic and makes it easy for non-programmers to iterate on prompt design without modifying Java code.

In the next chapter we explore the AgentScope framework, which takes the concept of LLM abstraction further by adding agent-oriented programming patterns like reasoning loops and tool calling on top of the basic completion interface we used here.

