package com.gemblogpro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents the JSON payload sent as the {@code blog} part of the
 * {@code multipart/form-data} body on {@code POST /api/blog/add}
 * (the frontend does {@code formData.append('blog', JSON.stringify(blog))}
 * in {@code AddBlog.jsx}).
 * <p>
 * Replaces the destructuring + manual required-field check in
 * {@code blogController.js}'s {@code addBlog}:
 * {@code if (!title || !description || !category || isPublished === undefined || isPublished === null)}.
 * The accompanying image file is a separate {@code MultipartFile} request
 * part, validated independently in the controller layer rather than as a
 * field on this DTO.
 * <p>
 * Phase 5: {@code title}/{@code subTitle}/{@code category} are now capped
 * to match the {@code Blog} entity's actual column lengths (500/500/100).
 * Without this, a title longer than the column would pass validation here
 * and only fail later as a raw Hibernate/MySQL data-truncation error,
 * surfacing to the client as an opaque 500 instead of a clear 400.
 */
public class BlogCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be at most 500 characters")
    private String title;

    @Size(max = 500, message = "Subtitle must be at most 500 characters")
    private String subTitle;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must be at most 100 characters")
    private String category;

    @NotNull(message = "isPublished is required")
    private Boolean isPublished;

    public BlogCreateRequest() {
    }

    public BlogCreateRequest(String title, String subTitle, String description,
                              String category, Boolean isPublished) {
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.category = category;
        this.isPublished = isPublished;
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

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }
}
