package com.gemblogpro.dto.response;

import java.util.List;

/**
 * Response body for {@code GET /api/admin/comments} and
 * {@code POST /api/blog/comments}. Replaces
 * {@code res.json({success:true, comments})} in {@code getAllComments} and
 * {@code getBlogComments}.
 */
public class CommentListResponse {

    private boolean success;
    private List<CommentResponse> comments;

    public CommentListResponse() {
    }

    public CommentListResponse(boolean success, List<CommentResponse> comments) {
        this.success = success;
        this.comments = comments;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<CommentResponse> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponse> comments) {
        this.comments = comments;
    }
}
