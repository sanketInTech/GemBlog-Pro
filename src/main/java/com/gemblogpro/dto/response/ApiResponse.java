package com.gemblogpro.dto.response;

/**
 * Generic {@code {success, message}} envelope, used for every endpoint that
 * doesn't need to return additional data - e.g. register, delete-comment,
 * approve-comment, delete blog, toggle-publish, add-comment. Mirrors the
 * plain {@code res.json({success, message})} calls scattered throughout
 * {@code adminController.js} / {@code blogController.js}.
 */
public class ApiResponse {

    private boolean success;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message);
    }

    public static ApiResponse failure(String message) {
        return new ApiResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
