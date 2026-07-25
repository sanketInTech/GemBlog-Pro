package com.gemblogpro.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemblogpro.dto.request.BlogCreateRequest;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.BlogDetailResponse;
import com.gemblogpro.dto.response.BlogListResponse;
import com.gemblogpro.dto.response.BlogResponse;
import com.gemblogpro.dto.response.UserSummaryResponse;
import com.gemblogpro.entity.Blog;
import com.gemblogpro.entity.User;
import com.gemblogpro.exception.ResourceNotFoundException;
import com.gemblogpro.exception.UnauthorizedActionException;
import com.gemblogpro.repository.BlogRepository;
import com.gemblogpro.repository.UserRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * Business logic for blog CRUD, replacing the corresponding functions in
 * {@code controllers/blogController.js}: {@code addBlog}, {@code getAllBlogs},
 * {@code getBlogByID}, {@code deleteBlogByID}, {@code togglePublish}.
 */
@Service
public class BlogService {

    private static final Logger log = LoggerFactory.getLogger(BlogService.class);

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final ImageKitService imageKitService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public BlogService(BlogRepository blogRepository,
                        UserRepository userRepository,
                        ImageKitService imageKitService,
                        ObjectMapper objectMapper,
                        Validator validator) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.imageKitService = imageKitService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Replaces {@code addBlog}. {@code blogJson} is the raw {@code blog}
     * form part (the frontend does
     * {@code formData.append('blog', JSON.stringify(blog))} in
     * {@code AddBlog.jsx}), deserialized and validated here rather than at
     * the controller boundary, since it arrives as a string inside a
     * multipart request rather than as the request body itself.
     */
    @Transactional
    public ApiResponse createBlog(String blogJson, MultipartFile image, Long authorId) {
        BlogCreateRequest request = parseBlogRequest(blogJson);
        validateBlogRequest(request);

        if (image == null || image.isEmpty()) {
            // Replaces `if (!imageFile) { return res.json({success:false, message:"Image file is required"}) }`.
            throw new IllegalArgumentException("Image file is required");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        String imageUrl = imageKitService.uploadBlogImage(image);

        Blog blog = new Blog(
                request.getTitle(),
                request.getSubTitle(),
                request.getDescription(),
                request.getCategory(),
                imageUrl,
                request.getIsPublished(),
                author);

        blogRepository.save(blog);
        log.info("Created blog id={} title='{}' authorId={}", blog.getId(), blog.getTitle(), authorId);

        return ApiResponse.success("Blog Added Successfully");
    }

    /** Replaces {@code getAllBlogs} (public listing, published only). */
    @Transactional(readOnly = true)
    public BlogListResponse getPublishedBlogs() {
        List<BlogResponse> blogs = blogRepository.findByIsPublishedTrue().stream()
                .map(this::toBlogResponse)
                .toList();
        return new BlogListResponse(true, blogs);
    }

    /** Replaces {@code getAllBlogsAdmin} (admin listing, all blogs). */
    @Transactional(readOnly = true)
    public BlogListResponse getAllBlogsForAdmin() {
        List<BlogResponse> blogs = blogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toBlogResponse)
                .toList();
        return new BlogListResponse(true, blogs);
    }

    /** Replaces {@code getBlogByID}. */
    @Transactional(readOnly = true)
    public BlogDetailResponse getBlogById(Long id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog Not Found"));
        return BlogDetailResponse.of(toBlogResponse(blog));
    }

    /** Replaces {@code deleteBlogByID}. */
    @Transactional
    public ApiResponse deleteBlog(Long id, Long requesterId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        if (!blog.getAuthor().getId().equals(requesterId)) {
            log.warn("User id={} attempted to delete blog id={} owned by user id={}",
                    requesterId, id, blog.getAuthor().getId());
            throw new UnauthorizedActionException("Unauthorized");
        }

        blogRepository.delete(blog);
        log.info("Deleted blog id={}", id);
        return ApiResponse.success("Blog Deleted Successfully");
    }

    /** Replaces {@code togglePublish}. */
    @Transactional
    public ApiResponse togglePublish(Long id, Long requesterId) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found"));

        if (!blog.getAuthor().getId().equals(requesterId)) {
            log.warn("User id={} attempted to toggle publish state on blog id={} owned by user id={}",
                    requesterId, id, blog.getAuthor().getId());
            throw new UnauthorizedActionException("Unauthorized");
        }

        blog.setPublished(!blog.isPublished());
        blogRepository.save(blog);
        log.info("Blog id={} publish state changed to {}", id, blog.isPublished());
        return ApiResponse.success("Blog status updated.");
    }

    /**
     * Maps a {@link Blog} entity to its response DTO, with {@code author}
     * populated inline - the DTO-mapping-layer equivalent of every
     * {@code .populate('author', 'name email')} call in the original
     * controllers. Package-visible so {@link DashboardService} can reuse it
     * for {@code recentBlogs} rather than duplicating the mapping.
     */
    BlogResponse toBlogResponse(Blog blog) {
        User author = blog.getAuthor();
        UserSummaryResponse authorSummary =
                new UserSummaryResponse(author.getId(), author.getName(), author.getEmail());

        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getSubTitle(),
                blog.getDescription(),
                blog.getCategory(),
                blog.getImage(),
                blog.isPublished(),
                authorSummary,
                blog.getCreatedAt(),
                blog.getUpdatedAt());
    }

    private BlogCreateRequest parseBlogRequest(String blogJson) {
        try {
            return objectMapper.readValue(blogJson, BlogCreateRequest.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid blog data: malformed JSON");
        }
    }

    private void validateBlogRequest(BlogCreateRequest request) {
        Set<ConstraintViolation<BlogCreateRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
