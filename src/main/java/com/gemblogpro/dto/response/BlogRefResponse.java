package com.gemblogpro.dto.response;

/**
 * Lightweight blog reference nested inside {@link CommentResponse}, mirroring
 * {@code .populate('blog')} in {@code adminController.js}'s
 * {@code getAllComments}. The frontend ({@code CommentTableItem.jsx}) only
 * reads {@code comment.blog?.title}, so only {@code id} and {@code title}
 * are projected here rather than the full {@link BlogResponse}.
 */
public class BlogRefResponse {

    private Long id;
    private String title;

    public BlogRefResponse() {
    }

    public BlogRefResponse(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
