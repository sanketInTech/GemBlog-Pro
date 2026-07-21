package com.gemblogpro.dto.response;

/**
 * Lightweight user projection used everywhere the Express app populated
 * {@code author} with only {@code 'name email'} (e.g.
 * {@code .populate('author', 'name email')} in {@code getAllBlogs},
 * {@code getAllBlogsAdmin}, {@code getBlogByID}, {@code getDashBoard}), and
 * for the {@code user} object returned on successful admin login.
 */
public class UserSummaryResponse {

    private Long id;
    private String name;
    private String email;

    public UserSummaryResponse() {
    }

    public UserSummaryResponse(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
