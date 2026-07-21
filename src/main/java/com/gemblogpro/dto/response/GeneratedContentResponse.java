package com.gemblogpro.dto.response;

/**
 * Response body for {@code POST /api/blog/generate}.
 * Replaces {@code res.json({success:true, content})} in
 * {@code blogController.js}'s {@code generateContent}.
 */
public class GeneratedContentResponse {

    private boolean success;
    private String content;

    public GeneratedContentResponse() {
    }

    public GeneratedContentResponse(boolean success, String content) {
        this.success = success;
        this.content = content;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
