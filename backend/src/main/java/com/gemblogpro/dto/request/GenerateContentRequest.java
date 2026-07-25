package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/blog/generate}.
 * Replaces {@code const {prompt} = req.body} in {@code blogController.js}'s
 * {@code generateContent}. The frontend ({@code AddBlog.jsx}) sends the
 * blog title as the prompt, so a generous but bounded cap keeps this from
 * being used to send arbitrarily large payloads to the Gemini API.
 */
public class GenerateContentRequest {

    @NotBlank(message = "Prompt is required")
    @Size(max = 500, message = "Prompt must be at most 500 characters")
    private String prompt;

    public GenerateContentRequest() {
    }

    public GenerateContentRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
