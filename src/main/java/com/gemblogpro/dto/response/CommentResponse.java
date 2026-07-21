package com.gemblogpro.dto.response;

import java.time.Instant;

/**
 * Represents a single comment as returned to the frontend. Used both inside
 * the public {@code getBlogComments} list (where {@code blog} is not needed
 * by the frontend but is included for consistency) and the admin
 * {@code getAllComments} list (where {@code blog.title} is displayed in
 * {@code CommentTableItem.jsx}).
 */
public class CommentResponse {

    private Long id;
    private String name;
    private String content;
    private boolean isApproved;
    private BlogRefResponse blog;
    private Instant createdAt;

    public CommentResponse() {
    }

    public CommentResponse(Long id, String name, String content, boolean isApproved,
                            BlogRefResponse blog, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.isApproved = isApproved;
        this.blog = blog;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public BlogRefResponse getBlog() {
        return blog;
    }

    public void setBlog(BlogRefResponse blog) {
        this.blog = blog;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
