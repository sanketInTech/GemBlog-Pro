package com.gemblogpro.dto.response;

import java.time.Instant;

/**
 * Represents a single blog post as returned to the frontend, with its
 * author populated inline - the DTO-level equivalent of every
 * {@code .populate('author', 'name email')} call in {@code blogController.js}
 * / {@code adminController.js}. Used both standalone (wrapped by
 * {@link BlogDetailResponse}) and inside lists (wrapped by
 * {@link BlogListResponse}, or nested in {@link DashboardResponse}).
 */
public class BlogResponse {

    private Long id;
    private String title;
    private String subTitle;
    private String description;
    private String category;
    private String image;
    private boolean isPublished;
    private UserSummaryResponse author;
    private Instant createdAt;
    private Instant updatedAt;

    public BlogResponse() {
    }

    public BlogResponse(Long id, String title, String subTitle, String description,
                         String category, String image, boolean isPublished,
                         UserSummaryResponse author, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.category = category;
        this.image = image;
        this.isPublished = isPublished;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public UserSummaryResponse getAuthor() {
        return author;
    }

    public void setAuthor(UserSummaryResponse author) {
        this.author = author;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
