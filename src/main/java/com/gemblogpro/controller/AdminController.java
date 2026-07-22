package com.gemblogpro.controller;

import com.gemblogpro.dto.request.CommentIdRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.BlogListResponse;
import com.gemblogpro.dto.response.CommentListResponse;
import com.gemblogpro.dto.response.DashboardResponse;
import com.gemblogpro.service.BlogService;
import com.gemblogpro.service.CommentService;
import com.gemblogpro.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Replaces the blog/comment-management + dashboard portion of
 * {@code routes/adminRoutes.js} + {@code controllers/adminController.js}
 * (registration/login/{@code /auth} live in {@code AuthController}, from
 * Phase 3 - both controllers share the {@code /api/admin} base path,
 * exactly as both sets of handlers share the same Express router).
 * Every endpoint here requires a valid JWT, matching the {@code auth}
 * middleware applied to each of these routes in the original app.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CommentService commentService;
    private final BlogService blogService;
    private final DashboardService dashboardService;

    public AdminController(CommentService commentService,
                            BlogService blogService,
                            DashboardService dashboardService) {
        this.commentService = commentService;
        this.blogService = blogService;
        this.dashboardService = dashboardService;
    }

    /** Replaces {@code adminRouter.get("/comments", auth, getAllComments)}. */
    @GetMapping("/comments")
    public ResponseEntity<CommentListResponse> getAllComments() {
        return ResponseEntity.ok(commentService.getAllComments());
    }

    /** Replaces {@code adminRouter.get("/blogs", auth, getAllBlogsAdmin)}. */
    @GetMapping("/blogs")
    public ResponseEntity<BlogListResponse> getAllBlogs() {
        return ResponseEntity.ok(blogService.getAllBlogsForAdmin());
    }

    /** Replaces {@code adminRouter.post("/delete-comment", auth, deleteCommentById)}. */
    @PostMapping("/delete-comment")
    public ResponseEntity<ApiResponse> deleteComment(@Valid @RequestBody CommentIdRequest request) {
        return ResponseEntity.ok(commentService.deleteComment(request.getId()));
    }

    /** Replaces {@code adminRouter.post("/approve-comment", auth, ApproveCommentById)}. */
    @PostMapping("/approve-comment")
    public ResponseEntity<ApiResponse> approveComment(@Valid @RequestBody CommentIdRequest request) {
        return ResponseEntity.ok(commentService.approveComment(request.getId()));
    }

    /** Replaces {@code adminRouter.get("/dashboard", auth, getDashBoard)}. */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}
