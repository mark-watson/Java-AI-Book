package com.markwatson.gemini;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GeminiCompletions.
 */
class GeminiCompletionsTest {

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
}
