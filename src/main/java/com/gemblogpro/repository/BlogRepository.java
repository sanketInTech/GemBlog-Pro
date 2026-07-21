package com.gemblogpro.repository;

import com.gemblogpro.entity.Blog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Replaces the direct Mongoose calls made against the {@code Blog} model in
 * {@code blogController.js} / {@code adminController.js}.
 * <p>
 * Every method that used to rely on Mongoose's {@code .populate('author', 'name email')}
 * instead uses {@link EntityGraph} (or an explicit {@code JOIN FETCH}) to eagerly
 * load {@code author} in the same query, avoiding N+1 selects.
 */
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    /**
     * Replaces {@code Blog.find({isPublished:true}).populate('author','name email')}
     * used by {@code getAllBlogs} (the public blog listing).
     */
    @EntityGraph(attributePaths = "author")
    List<Blog> findByIsPublishedTrue();

    /**
     * Replaces {@code Blog.find({}).populate('author','name email').sort({createdAt:-1})}
     * used by {@code getAllBlogsAdmin}.
     */
    @EntityGraph(attributePaths = "author")
    List<Blog> findAllByOrderByCreatedAtDesc();

    /**
     * Replaces {@code Blog.findById(id).populate('author','name email')}
     * used by {@code getBlogByID}.
     */
    @EntityGraph(attributePaths = "author")
    Optional<Blog> findById(Long id);

    /**
     * Replaces the recent-5 query used to populate {@code dashboardData.recentBlogs}
     * in {@code getDashBoard} (previously {@code .sort({createdAt:-1}).limit(5)}).
     */
    @EntityGraph(attributePaths = "author")
    List<Blog> findTop5ByOrderByCreatedAtDesc();

    /** Replaces {@code Blog.countDocuments()} used by {@code getDashBoard}. */
    long count();

    /** Replaces {@code Blog.countDocuments({isPublished:false})} used by {@code getDashBoard}. */
    long countByIsPublishedFalse();

    /**
     * Explicit JPQL alternative to the {@code @EntityGraph} approach above,
     * kept here as a documented fallback in case a future query needs finer
     * control than {@code @EntityGraph} offers (e.g. combining the join with
     * additional filtering).
     */
    @Query("select b from Blog b join fetch b.author where b.isPublished = true")
    List<Blog> findPublishedBlogsWithAuthor();

}
