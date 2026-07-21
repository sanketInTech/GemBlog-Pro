package com.gemblogpro.repository;

import com.gemblogpro.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Replaces the direct Mongoose calls made against the {@code Comment} model
 * in {@code blogController.js} / {@code adminController.js}.
 * <p>
 * {@code .populate('blog')} calls are replaced with {@link EntityGraph} to
 * eagerly fetch the parent {@code Blog} (and, transitively, the fields the
 * frontend reads via {@code comment.blog?.title}) in a single query.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Replaces {@code Comment.find({}).populate('blog').sort({createdAt:-1})}
     * used by {@code getAllComments}.
     */
    @EntityGraph(attributePaths = "blog")
    List<Comment> findAllByOrderByCreatedAtDesc();

    /**
     * Replaces {@code Comment.find({blog:blogId, isApproved:true}).sort({createdAt:-1})}
     * used by {@code getBlogComments} (public, approved comments for one blog post).
     */
    List<Comment> findByBlogIdAndIsApprovedTrueOrderByCreatedAtDesc(Long blogId);

    /** Replaces {@code Comment.countDocuments({isApproved:true})} used by {@code getDashBoard}. */
    long countByIsApprovedTrue();

}
