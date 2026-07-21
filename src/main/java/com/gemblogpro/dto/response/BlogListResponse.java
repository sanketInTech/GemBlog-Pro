package com.gemblogpro.dto.response;

import java.util.List;

/**
 * Response body for {@code GET /api/blog/all} and {@code GET /api/admin/blogs}.
 * Replaces {@code res.json({success:true, blogs})} in {@code getAllBlogs}
 * and {@code getAllBlogsAdmin}.
 */
public class BlogListResponse {

    private boolean success;
    private List<BlogResponse> blogs;

    public BlogListResponse() {
    }

    public BlogListResponse(boolean success, List<BlogResponse> blogs) {
        this.success = success;
        this.blogs = blogs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<BlogResponse> getBlogs() {
        return blogs;
    }

    public void setBlogs(List<BlogResponse> blogs) {
        this.blogs = blogs;
    }
}
