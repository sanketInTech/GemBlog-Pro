package com.gemblogpro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Replaces {@code config/gemini.js}:
 * <pre>
 *   const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });
 * </pre>
 * As with {@link ImageKitConfig}, {@code GeminiContentService} calls the
 * Gemini REST API directly via the shared {@code RestTemplate} rather than
 * the Google Gen AI Java SDK, so this class holds the equivalent client
 * configuration (API key, model name) sourced from {@code application.yml}.
 * {@code model} defaults to {@code gemini-3.5-flash}, matching the
 * hardcoded model name used in the original {@code main()} function.
 */
@Configuration
public class GeminiConfig {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.5-flash-lite}")
    private String model;

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }
}
