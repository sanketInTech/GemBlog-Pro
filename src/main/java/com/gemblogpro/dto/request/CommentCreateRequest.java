package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/blog/add-comment}.
 * Replaces {@code const {blog, name, content} = req.body} in
 * {@code blogController.js}'s {@code addComment}. Note {@code blog} in the
 * original Mongo document is renamed {@code blogId} here to reflect that it
 * is now a foreign key value rather than an embedded reference object.
 */
public class CommentCreateRequest {

    @NotNull(message = "blogId is required")
    private Long blogId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Content is required")
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
