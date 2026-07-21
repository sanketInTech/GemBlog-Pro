package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/blog/generate}.
 * Replaces {@code const {prompt} = req.body} in {@code blogController.js}'s
 * {@code generateContent}. The frontend ({@code AddBlog.jsx}) sends the
 * blog title as the prompt.
 */
public class GenerateContentRequest {

    @NotBlank(message = "Prompt is required")
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
