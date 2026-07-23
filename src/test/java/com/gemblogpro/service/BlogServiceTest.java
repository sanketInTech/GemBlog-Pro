package com.gemblogpro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemblogpro.dto.response.ApiResponse;
import com.gemblogpro.dto.response.BlogListResponse;
import com.gemblogpro.entity.Blog;
import com.gemblogpro.entity.User;
import com.gemblogpro.exception.ResourceNotFoundException;
import com.gemblogpro.exception.UnauthorizedActionException;
import com.gemblogpro.repository.BlogRepository;
import com.gemblogpro.repository.UserRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BlogService}, focused on the two rules that are
 * easiest to silently break in a refactor: only a blog's own author can
 * delete/toggle-publish it, and unknown IDs 404 rather than throwing an
 * unhandled exception. A real {@link Validator} is used (not mocked) for
 * {@link #getPublishedBlogs_mapsAuthorInline} so the {@code author}
 * population behaves exactly as it does at runtime.
 */
@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageKitService imageKitService;

    private BlogService blogService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        blogService = new BlogService(blogRepository, userRepository, imageKitService, objectMapper, validator);
    }

    @Test
    void deleteBlog_throwsResourceNotFoundException_whenBlogDoesNotExist() {
        when(blogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.deleteBlog(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Blog not found");
    }

    @Test
    void deleteBlog_throwsUnauthorizedActionException_whenRequesterIsNotAuthor() {
        User author = userWithId(1L, "Author One", "author@example.com");
        Blog blog = blogWithIdAndAuthor(10L, author);
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));

        assertThatThrownBy(() -> blogService.deleteBlog(10L, 2L))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessage("Unauthorized");

        verify(blogRepository, org.mockito.Mockito.never()).delete(blog);
    }

    @Test
    void deleteBlog_deletesBlog_whenRequesterIsAuthor() {
        User author = userWithId(1L, "Author One", "author@example.com");
        Blog blog = blogWithIdAndAuthor(10L, author);
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));

        ApiResponse response = blogService.deleteBlog(10L, 1L);

        assertThat(response.isSuccess()).isTrue();
        verify(blogRepository).delete(blog);
    }

    @Test
    void togglePublish_flipsPublishedFlag_whenRequesterIsAuthor() {
        User author = userWithId(1L, "Author One", "author@example.com");
        Blog blog = blogWithIdAndAuthor(10L, author);
        assertThat(blog.isPublished()).isFalse();
        when(blogRepository.findById(10L)).thenReturn(Optional.of(blog));

        blogService.togglePublish(10L, 1L);

        assertThat(blog.isPublished()).isTrue();
        verify(blogRepository).save(blog);
    }

    @Test
    void createBlog_throwsIllegalArgumentException_whenImageMissing() {
        String blogJson = "{\"title\":\"t\",\"description\":\"d\",\"category\":\"c\",\"isPublished\":true}";

        assertThatThrownBy(() -> blogService.createBlog(blogJson, null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image file is required");

        verifyNoInteractions(imageKitService);
    }

    @Test
    void getPublishedBlogs_mapsAuthorInline() {
        User author = userWithId(1L, "Author One", "author@example.com");
        Blog blog = blogWithIdAndAuthor(10L, author);
        when(blogRepository.findByIsPublishedTrue()).thenReturn(List.of(blog));

        BlogListResponse response = blogService.getPublishedBlogs();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getBlogs()).hasSize(1);
        assertThat(response.getBlogs().get(0).getAuthor().getEmail()).isEqualTo("author@example.com");
    }

    private static User userWithId(Long id, String name, String email) {
        User user = new User(name, email, "hashed-password");
        user.setId(id);
        return user;
    }

    private static Blog blogWithIdAndAuthor(Long id, User author) {
        Blog blog = new Blog("Title", "Subtitle", "Description", "Category", "https://example.com/image.jpg",
                false, author);
        blog.setId(id);
        return blog;
    }
}
