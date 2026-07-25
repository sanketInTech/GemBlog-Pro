package com.gemblogpro.service;

import com.gemblogpro.dto.response.BlogResponse;
import com.gemblogpro.dto.response.DashboardResponse;
import com.gemblogpro.repository.BlogRepository;
import com.gemblogpro.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces {@code getDashBoard} from {@code controllers/adminController.js}:
 * <pre>
 *   const recentBlogs = await Blog.find({}).populate('author','name email').sort({createdAt:-1}).limit(5);
 *   const blogs = await Blog.countDocuments();
 *   const comments = await Comment.countDocuments({ isApproved: true });
 *   const drafts = await Blog.countDocuments({isPublished: false});
 * </pre>
 */
@Service
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final BlogService blogService;

    public DashboardService(BlogRepository blogRepository,
                             CommentRepository commentRepository,
                             BlogService blogService) {
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.blogService = blogService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardStats() {
        long blogs = blogRepository.count();
        long comments = commentRepository.countByIsApprovedTrue();
        long drafts = blogRepository.countByIsPublishedFalse();

        // Reuses BlogService's entity->DTO mapping (including populated
        // author) instead of duplicating it here.
        List<BlogResponse> recentBlogs = blogRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(blogService::toBlogResponse)
                .toList();

        DashboardResponse.DashboardData data =
                new DashboardResponse.DashboardData(blogs, comments, drafts, recentBlogs);

        log.debug("Dashboard stats computed: blogs={} approvedComments={} drafts={}", blogs, comments, drafts);
        return new DashboardResponse(true, data);
    }
}
