package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request body for {@code POST /api/admin/delete-comment} and
 * {@code POST /api/admin/approve-comment}.
 * Replaces {@code const {id} = req.body} in {@code adminController.js}'s
 * {@code deleteCommentById} and {@code ApproveCommentById}.
 */
public class CommentIdRequest {

    @NotNull(message = "id is required")
    private Long id;

    public CommentIdRequest() {
    }

    public CommentIdRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
