package com.gemblogpro.dto.response;

/**
 * Response body for {@code GET /api/blog/{blogId}}.
 * Replaces {@code res.json({success:true, blog})} in {@code getBlogByID}
 * (and the {@code {success:false, message:"Blog Not Found"}} failure case,
 * represented here by leaving {@code blog} null and {@code message} set).
 */
public class BlogDetailResponse {

    private boolean success;
    private BlogResponse blog;
    private String message;

    public BlogDetailResponse() {
    }

    public BlogDetailResponse(boolean success, BlogResponse blog, String message) {
        this.success = success;
        this.blog = blog;
        this.message = message;
    }

    public static BlogDetailResponse of(BlogResponse blog) {
        return new BlogDetailResponse(true, blog, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public BlogResponse getBlog() {
        return blog;
    }

    public void setBlog(BlogResponse blog) {
        this.blog = blog;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
