package com.gemblogpro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemblogpro.config.GeminiConfig;
import com.gemblogpro.exception.ExternalServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Replaces {@code config/gemini.js}:
 * <pre>
 *   async function main(prompt) {
 *     const response = await ai.models.generateContent({ model: "gemini-2.5-flash", contents: prompt });
 *     return response.text;
 *   }
 * </pre>
 * and its call site in {@code blogController.js}'s {@code generateContent}:
 * <pre>
 *   const content = await main(prompt + " Generate a blog content for this topic in simple text format");
 * </pre>
 * Calls the Gemini {@code generateContent} REST endpoint directly rather
 * than the Google Gen AI Java SDK, per the architecture document's explicit
 * REST-client allowance.
 */
@Service
public class GeminiContentService {

    private static final String ENDPOINT_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    /** Preserved exactly from the original app's fixed instruction suffix. */
    private static final String INSTRUCTION_SUFFIX =
            " Generate a blog content for this topic in simple text format";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final GeminiConfig geminiConfig;

    public GeminiContentService(RestTemplate restTemplate, ObjectMapper objectMapper, GeminiConfig geminiConfig) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.geminiConfig = geminiConfig;
    }

    /**
     * Generates blog content for the given prompt (the blog title, as sent
     * by {@code AddBlog.jsx}'s "Generate with AI" button).
     */
    public String generateBlogContent(String prompt) {
        String fullPrompt = prompt + INSTRUCTION_SUFFIX;
        String url = String.format(ENDPOINT_TEMPLATE, geminiConfig.getModel(), geminiConfig.getApiKey());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", fullPrompt))))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, new HttpEntity<>(requestBody, headers), String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (textNode.isMissingNode()) {
                throw new ExternalServiceException("Gemini returned an unexpected response");
            }

            return textNode.asText();

        } catch (RestClientException | IOException ex) {
            throw new ExternalServiceException("Content generation failed: " + ex.getMessage());
        }
    }
}
