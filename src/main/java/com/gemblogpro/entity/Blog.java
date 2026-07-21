package com.gemblogpro.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA equivalent of {@code server/models/Blog.js}.
 * <p>
 * Replaces the Mongoose {@code Blog} model. {@code author} was a Mongo
 * {@code ObjectId} reference resolved manually via {@code .populate()}
 * calls in the Express controllers; here it is a real foreign key via
 * {@link #author}, and fetching {@code author.getName()} / {@code
 * author.getEmail()} at the DTO-mapping layer <em>is</em> the populate step.
 */
@Entity
@Table(name = "blogs")
@EntityListeners(AuditingEntityListener.class)
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "sub_title", length = 500)
    private String subTitle;

    /** Rich-text HTML produced by the Quill editor / Gemini generation. */
    @Lob
    @Column(name = "description", nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    /** Final optimized ImageKit URL (post {@code tr=q-auto,f-webp,w-1280} transform). */
    @Column(name = "image", nullable = false, length = 1000)
    private String image;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    /**
     * Owning side of the "one User authors many Blogs" relationship.
     * Replaces {@code author: {type: ObjectId, ref: "User"}} in Blog.js.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /**
     * Owning side is {@link Comment#blog}; this is the inverse side.
     * {@code cascade = ALL} + {@code orphanRemoval = true} means deleting a
     * Blog deletes its Comments with it - see the architecture document's
     * note on this being a deliberate, approved behavior difference from
     * the current Express app (which leaves comments orphaned today).
     */
    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Blog() {
        // Required by JPA
    }

    public Blog(String title, String subTitle, String description, String category,
                String image, boolean isPublished, User author) {
        this.title = title;
        this.subTitle = subTitle;
        this.description = description;
        this.category = category;
        this.image = image;
        this.isPublished = isPublished;
        this.author = author;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Blog)) return false;
        Blog blog = (Blog) o;
        return id != null && id.equals(blog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Blog{id=" + id + ", title='" + title + "', category='" + category
                + "', isPublished=" + isPublished + "}";
    }
}
