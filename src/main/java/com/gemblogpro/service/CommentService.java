package com.gemblogpro.service;

import com.gemblogpro.dto.request.CommentCreateRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.BlogRefResponse;
import com.gemblogpro.dto.response.CommentListResponse;
import com.gemblogpro.dto.response.CommentResponse;
import com.gemblogpro.entity.Blog;
import com.gemblogpro.entity.Comment;
import com.gemblogpro.exception.ResourceNotFoundException;
import com.gemblogpro.repository.BlogRepository;
import com.gemblogpro.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for comment CRUD and moderation, replacing
 * {@code addComment} and {@code getBlogComments} from
 * {@code controllers/blogController.js}, plus {@code getAllComments},
 * {@code deleteCommentById}, and {@code ApproveCommentById} from
 * {@code controllers/adminController.js}.
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;

    public CommentService(CommentRepository commentRepository, BlogRepository blogRepository) {
        this.commentRepository = commentRepository;
        this.blogRepository = blogRepository;
    }

    /**
     * Replaces {@code addComment}. One necessary behavior difference from
     * the original: Mongoose's {@code Comment.create({blog, name, content})}
     * would happily store a comment referencing a nonexistent blog ID (no
     * validation), whereas the relational {@code blog_id} foreign key
     * requires the referenced blog to actually exist - so this now looks
     * the blog up first and returns a clean 404 if it doesn't, rather than
     * letting a raw SQL constraint violation surface as a 500.
     */
    @Transactional
    public ApiResponse addComment(CommentCreateRequest request) {
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        Comment comment = new Comment(blog, request.getName(), request.getContent());
        commentRepository.save(comment);

        return ApiResponse.success("Comment Added For Review.");
    }

    /** Replaces {@code getBlogComments} (public, approved comments for one blog). */
    @Transactional(readOnly = true)
    public CommentListResponse getApprovedComments(Long blogId) {
        List<CommentResponse> comments = commentRepository
                .findByBlogIdAndIsApprovedTrueOrderByCreatedAtDesc(blogId).stream()
                .map(this::toCommentResponse)
                .toList();
        return new CommentListResponse(true, comments);
    }

    /** Replaces {@code getAllComments} (admin, every comment regardless of approval). */
    @Transactional(readOnly = true)
    public CommentListResponse getAllComments() {
        List<CommentResponse> comments = commentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toCommentResponse)
                .toList();
        return new CommentListResponse(true, comments);
    }

    /**
     * Replaces {@code ApproveCommentById}. Deliberate improvement: the
     * original {@code Comment.findByIdAndUpdate(id, {isApproved:true})}
     * silently no-ops (still returns {@code {success:true}}) if the ID
     * doesn't exist. This now returns a proper 404 in that case - flagged
     * here since it's an observable behavior change, even though it's a
     * more correct one.
     */
    @Transactional
    public ApiResponse approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        comment.setApproved(true);
        commentRepository.save(comment);
        return ApiResponse.success("Comment Approved Successfully.");
    }

    /**
     * Replaces {@code deleteCommentById}. Same deliberate improvement noted
     * on {@link #approveComment(Long)} applies here: unknown IDs now 404
     * instead of silently "succeeding".
     */
    @Transactional
    public ApiResponse deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        commentRepository.delete(comment);
        return ApiResponse.success("Comment Deleted Successfully");
    }

    private CommentResponse toCommentResponse(Comment comment) {
        Blog blog = comment.getBlog();
        BlogRefResponse blogRef = new BlogRefResponse(blog.getId(), blog.getTitle());

        return new CommentResponse(
                comment.getId(),
                comment.getName(),
                comment.getContent(),
                comment.isApproved(),
                blogRef,
                comment.getCreatedAt());
    }
}
