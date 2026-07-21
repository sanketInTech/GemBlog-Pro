package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/blog/delete} and
 * {@code POST /api/blog/toggle-publish}.
 * Replaces {@code const {id} = req.body} in {@code blogController.js}'s
 * {@code deleteBlogByID} and {@code togglePublish}.
 */
public class BlogIdRequest {

    @NotNull(message = "id is required")
    private Long id;

    public BlogIdRequest() {
    }

    public BlogIdRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
