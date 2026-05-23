package com.markwatson.agentscope;

import io.agentscope.core.model.GeminiChatModel;

/**
 * Shared configuration for Gemini-based examples.
 *
 * <p>Centralises API-key validation and model construction so that every
 * example in this project uses the same defaults.
 */
public final class GeminiConfig {

    /**
     * Default Gemini model identifier.
     *
     * <p>As of mid-2026 Google also offers {@code gemini-3.5-flash} with
     * improved agentic capabilities — swap this constant if you have access.
     */
    public static final String MODEL_NAME = "gemini-2.5-flash";

    private GeminiConfig() { /* utility class */ }

    /**
     * Returns the value of the {@code GEMINI_API_KEY} environment variable,
     * or terminates the JVM with a helpful error message if it is unset.
     */
    public static String requireApiKey() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("ERROR: GEMINI_API_KEY environment variable is not set.");
            System.exit(1);
        }
        return apiKey;
    }

    /**
     * Convenience factory that builds a {@link GeminiChatModel} using the
     * standard API key and default model name.
     */
    public static GeminiChatModel createModel() {
        return GeminiChatModel.builder()
                .apiKey(requireApiKey())
                .modelName(MODEL_NAME)
                .build();
    }
}
