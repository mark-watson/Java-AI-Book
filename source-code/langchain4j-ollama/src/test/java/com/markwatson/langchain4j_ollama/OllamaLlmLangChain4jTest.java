package com.markwatson.langchain4j_ollama;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OllamaLlmLangChain4j.
 * Requires a running Ollama server with the specified models pulled.
 */
@Tag("integration")
class OllamaLlmLangChain4jTest {

    @Test
    @DisplayName("Simple completion with Ollama model")
    void testCompletion() {
        String result = OllamaLlmLangChain4j.getCompletion(
                "Translate the following English text to French: 'Hello, how are you?'",
                "gemma3:1b");

        System.out.println("\n\n&&&&&&&&&&\n\ncompletion: " + result);
        assertNotNull(result, "Completion result should not be null");
        assertFalse(result.isBlank(), "Completion result should not be blank");
    }

    @Test
    @DisplayName("Two-shot template extraction")
    void testTwoShotTemplate() throws Exception {
        String inputText = "Mark Smith enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";
        String prompt0 = OllamaLlmLangChain4j.readFileToString("../prompts/two-shot-2-var.txt");
        System.out.println("prompt0: " + prompt0);

        String prompt = OllamaLlmLangChain4j.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);

        String result = OllamaLlmLangChain4j.getCompletion(prompt, "gemma3:1b");
        System.out.println("two shot extraction completion: " + result);

        assertNotNull(result, "Two-shot extraction result should not be null");
        assertFalse(result.isBlank(), "Two-shot extraction result should not be blank");
    }

    @Test
    @DisplayName("Text summarization")
    void testSummarization() throws Exception {
        String inputText = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.";
        String prompt0 = OllamaLlmLangChain4j.readFileToString("../prompts/summarization_prompt.txt");
        System.out.println("prompt0: " + prompt0);

        String prompt = OllamaLlmLangChain4j.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);

        String result = OllamaLlmLangChain4j.getCompletion(prompt, "gemma3:1b");
        System.out.println("summarization completion: " + result);

        assertNotNull(result, "Summarization result should not be null");
        assertFalse(result.isBlank(), "Summarization result should not be blank");
    }
}
