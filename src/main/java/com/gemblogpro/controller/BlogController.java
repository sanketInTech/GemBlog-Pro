package com.gemblogpro.controller;

import com.gemblogpro.dto.request.BlogIdRequest;
import com.gemblogpro.dto.request.CommentCreateRequest;
import com.gemblogpro.dto.request.CommentsByBlogRequest;
import com.gemblogpro.dto.request.GenerateContentRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.BlogDetailResponse;
import com.gemblogpro.dto.response.BlogListResponse;
import com.gemblogpro.dto.response.CommentListResponse;
import com.gemblogpro.dto.response.GeneratedContentResponse;
import com.gemblogpro.security.UserPrincipal;
import com.gemblogpro.service.BlogService;
import com.gemblogpro.service.CommentService;
import com.gemblogpro.service.GeminiContentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Replaces {@code routes/blogRoutes.js} + the corresponding functions in
 * {@code controllers/blogController.js}. Route paths, HTTP methods, and
 * JSON field names are unchanged so {@code AddBlog.jsx}, {@code Blog.jsx},
 * {@code BlogList.jsx}, and {@code AppContext.jsx} keep working without
 * modification. Per the approved architecture revision, endpoints that
 * create a resource now return {@code 201 Created} instead of {@code 200}.
 * <p>
 * {@code @Validated} at the class level activates method-parameter
 * constraints (e.g. {@code @Positive} on {@link #getBlogById}'s
 * {@code blogId}) - without it, constraints on a bare {@code @PathVariable}
 * are silently ignored rather than enforced.
 */
@RestController
@RequestMapping("/api/blog")
@Validated
public class BlogController {

    private static final Logger log = LoggerFactory.getLogger(BlogController.class);

    private final BlogService blogService;
    private final CommentService commentService;
    private final GeminiContentService geminiContentService;

    public BlogController(BlogService blogService,
                           CommentService commentService,
                           GeminiContentService geminiContentService) {
        this.blogService = blogService;
        this.commentService = commentService;
        this.geminiContentService = geminiContentService;
    }

    /**
     * Replaces {@code blogRouter.post("/add", auth, upload.single('image'), addBlog)}.
     * {@code blog} arrives as a JSON string form part (matching
     * {@code formData.append('blog', JSON.stringify(blog))} in
     * {@code AddBlog.jsx}); {@code image} is the accompanying file part.
     * {@code UserPrincipal} (the caller's identity, resolved from the JWT
     * by {@code JwtAuthenticationFilter}) supplies the blog's author -
     * replacing {@code author: req.user.userId} in the original.
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> addBlog(
            @RequestParam("blog") String blogJson,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserPrincipal principal) {

        log.info("Creating blog for author id={}", principal.getId());
        ApiResponse response = blogService.createBlog(blogJson, image, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Replaces {@code blogRouter.get('/all', getAllBlogs)} (public, published blogs only). */
    @GetMapping("/all")
    public ResponseEntity<BlogListResponse> getAllBlogs() {
        return ResponseEntity.ok(blogService.getPublishedBlogs());
    }

    /** Replaces {@code blogRouter.post('/delete', auth, deleteBlogByID)}. */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteBlog(@Valid @RequestBody BlogIdRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Deleting blog id={} requested by user id={}", request.getId(), principal.getId());
        return ResponseEntity.ok(blogService.deleteBlog(request.getId(), principal.getId()));
    }

    /** Replaces {@code blogRouter.post('/toggle-publish', auth, togglePublish)}. */
    @PostMapping("/toggle-publish")
    public ResponseEntity<ApiResponse> togglePublish(@Valid @RequestBody BlogIdRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Toggling publish state for blog id={} requested by user id={}", request.getId(), principal.getId());
        return ResponseEntity.ok(blogService.togglePublish(request.getId(), principal.getId()));
    }

    /** Replaces {@code blogRouter.post('/add-comment', addComment)} (public). */
    @PostMapping("/add-comment")
    public ResponseEntity<ApiResponse> addComment(@Valid @RequestBody CommentCreateRequest request) {
        log.info("Adding comment for blog id={}", request.getBlogId());
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.addComment(request));
    }

    /** Replaces {@code blogRouter.post('/comments', getBlogComments)} (public). */
    @PostMapping("/comments")
    public ResponseEntity<CommentListResponse> getBlogComments(@Valid @RequestBody CommentsByBlogRequest request) {
        return ResponseEntity.ok(commentService.getApprovedComments(request.getBlogId()));
    }

    /** Replaces {@code blogRouter.post('/generate', auth, generateContent)}. */
    @PostMapping("/generate")
    public ResponseEntity<GeneratedContentResponse> generate(@Valid @RequestBody GenerateContentRequest request) {
        log.info("Generating AI content for prompt of length={}", request.getPrompt().length());
        String content = geminiContentService.generateBlogContent(request.getPrompt());
        return ResponseEntity.ok(new GeneratedContentResponse(true, content));
    }

    /**
     * Replaces {@code blogRouter.get('/:blogId', getBlogByID)} (public).
     * {@code @Positive} (Phase 5 addition) rejects {@code 0} or negative
     * IDs at the framework boundary with a clean 400, instead of letting
     * them reach the repository as a well-formed but nonsensical query that
     * simply returns "not found".
     */
    @GetMapping("/{blogId}")
    public ResponseEntity<BlogDetailResponse> getBlogById(
            @PathVariable @Positive(message = "blogId must be a positive number") Long blogId) {
        return ResponseEntity.ok(blogService.getBlogById(blogId));
    }
}
