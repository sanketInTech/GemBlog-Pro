package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/blog/add-comment}.
 * Replaces {@code const {blog, name, content} = req.body} in
 * {@code blogController.js}'s {@code addComment}. Note {@code blog} in the
 * original Mongo document is renamed {@code blogId} here to reflect that it
 * is now a foreign key value rather than an embedded reference object.
 */
public class CommentCreateRequest {

    @NotNull(message = "blogId is required")
    @Positive(message = "blogId must be a positive number")
    private Long blogId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must be at most 5000 characters")
    private String content;

    public CommentCreateRequest() {
    }

    public CommentCreateRequest(Long blogId, String name, String content) {
        this.blogId = blogId;
        this.name = name;
        this.content = content;
    }

    public Long getBlogId() {
        return blogId;
    }

    public void setBlogId(Long blogId) {
        this.blogId = blogId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
