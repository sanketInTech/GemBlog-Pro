package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/blog/comments}.
 * Replaces {@code const {blogId} = req.body} in {@code blogController.js}'s
 * {@code getBlogComments}.
 */
public class CommentsByBlogRequest {

    @NotNull(message = "blogId is required")
    private Long blogId;

    public CommentsByBlogRequest() {
    }

    public CommentsByBlogRequest(Long blogId) {
        this.blogId = blogId;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }
}
