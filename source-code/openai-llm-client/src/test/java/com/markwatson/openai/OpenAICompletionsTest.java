package com.markwatson.openai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for OpenAICompletions.
 */
class OpenAICompletionsTest {

    @Test
    @DisplayName("Basic chat completion returns non-empty response")
    void testCompletion() {
        var result = OpenAICompletions.getCompletion(
                "Translate the following English text to French: 'Hello, how are you?'");
        System.out.println("completion: " + result);
        assertNotNull(result, "Completion result should not be null");
        assertFalse(result.isEmpty(), "Completion result should not be empty");
    }

    @Test
    @DisplayName("Two-shot template extraction returns non-empty response")
    void testTwoShotTemplate() throws Exception {
        var inputText = "Mark Johnson enjoys living in Berkeley California at 102 Dunston Street and use mjess@foobar.com for contacting him.";
        var prompt0 = OpenAICompletions.readFileToString("../prompts/two-shot-2-var.txt");
        System.out.println("prompt0: " + prompt0);
        var prompt = OpenAICompletions.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);
        var result = OpenAICompletions.getCompletion(prompt);
        System.out.println("two shot extraction completion: " + result);
        assertNotNull(result, "Two-shot result should not be null");
        assertFalse(result.isEmpty(), "Two-shot result should not be empty");
    }

    @Test
    @DisplayName("Summarization returns non-empty response")
    void testSummarization() throws Exception {
        var inputText = "Jupiter is the fifth planet from the Sun and the largest in the Solar System. It is a gas giant with a mass one-thousandth that of the Sun, but two-and-a-half times that of all the other planets in the Solar System combined. Jupiter is one of the brightest objects visible to the naked eye in the night sky, and has been known to ancient civilizations since before recorded history. It is named after the Roman god Jupiter.[19] When viewed from Earth, Jupiter can be bright enough for its reflected light to cast visible shadows,[ and is on average the third-brightest natural object in the night sky after the Moon and Venus.";
        var prompt0 = OpenAICompletions.readFileToString("../prompts/summarization_prompt.txt");
        System.out.println("prompt0: " + prompt0);
        var prompt = OpenAICompletions.promptVar(prompt0, "{input_text}", inputText);
        System.out.println("prompt: " + prompt);
        var result = OpenAICompletions.getCompletion(prompt);
        System.out.println("summarization completion: " + result);
        assertNotNull(result, "Summarization result should not be null");
        assertFalse(result.isEmpty(), "Summarization result should not be empty");
    }
}
