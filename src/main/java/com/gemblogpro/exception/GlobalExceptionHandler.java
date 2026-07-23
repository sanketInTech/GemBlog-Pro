package com.gemblogpro.exception;

import com.gemblogpro.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Centralizes what, in the Express app, was a
 * {@code try { ... } catch(error) { res.json({success:false, message: error.message}) }}
 * block repeated in nearly every controller function.
 * <p>
 * Per the approved architecture revision, responses now use standard HTTP
 * status codes (200/201/400/401/403/404/500) instead of always returning
 * 200 with a {@code success:false} body. The JSON body shape itself
 * ({@code {success, message}}) is unchanged, so the frontend's existing
 * {@code if(data.success) ... else toast.error(data.message)} pattern keeps
 * working for every 2xx response; for non-2xx responses axios raises an
 * error and the frontend's existing {@code catch(error){ toast.error(error.message) }}
 * still surfaces a visible toast (with a more generic message than before -
 * see the Phase 3 summary notes for details).
 * <p>
 * Every handler logs via SLF4J: client-error branches (400/401/403/404) at
 * {@code WARN} with just the message (expected, routine traffic - not worth
 * a stack trace), unexpected/server-error branches at {@code ERROR} with
 * the full exception so it's diagnosable from logs alone.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** Bean Validation failures on @Valid request DTOs -> 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        log.warn("Validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(message));
    }

    /** Email already registered -> 400. */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
    }

    /** Wrong email/password on login -> 401. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid credentials on login attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(ex.getMessage()));
    }

    /**
     * Raised by {@code AuthenticationManager.authenticate(...)} itself
     * (e.g. if it rejects before {@code AuthService} gets a chance to throw
     * its own {@link InvalidCredentialsException}) -> 401, same message
     * used everywhere else for bad login attempts.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials on login attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("Invalid credentials"));
    }

    /**
     * Requested entity not found -> 404. Used by {@code getBlogByID},
     * {@code deleteBlogByID}, {@code togglePublish} (blog lookups), and
     * comment/comment-approval lookups.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    /**
     * Manual Bean Validation failures on a request DTO that was
     * hand-deserialized rather than bound via {@code @Valid @RequestBody}
     * (specifically {@code BlogCreateRequest}, which arrives as a JSON
     * string inside a {@code multipart/form-data} part rather than as the
     * request body itself) -> 400. Replaces the
     * {@code if (!title || !description || !category || isPublished === undefined || isPublished === null)}
     * check in {@code addBlog}.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse("Validation failed");
        log.warn("Constraint violation: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(message));
    }

    /**
     * Covers ad-hoc input checks that don't fit the Bean Validation
     * pipeline, notably the missing-image-file check in {@code addBlog} ->
     * 400. Replaces {@code if (!imageFile) { return res.json({success:false, message:"Image file is required"}) }}.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
    }

    /**
     * Multipart upload exceeding {@code spring.servlet.multipart.max-file-size}
     * -> 400. Replaces the inline multer error-handling wrapper in
     * {@code blogRoutes.js}: {@code upload.single('image')(req,res,(err)=>{ if(err) return res.status(400).json(...) })}.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("Upload rejected - file too large");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Uploaded file is too large"));
    }

    /**
     * A required {@code multipart/form-data} part is missing entirely -
     * e.g. {@code POST /api/blog/add} without the {@code blog} part -> 400.
     * (Phase 5 addition: previously fell through to the generic 500
     * handler, which is the wrong status code for a client input error.)
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiResponse> handleMissingPart(MissingServletRequestPartException ex) {
        log.warn("Missing required request part: {}", ex.getRequestPartName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("Missing required field: " + ex.getRequestPartName()));
    }

    /**
     * A {@code @RequestBody} could not be parsed as JSON (empty, malformed,
     * or wrong type) -> 400. (Phase 5 addition: same rationale as
     * {@link #handleMissingPart} - this previously fell through to 500.)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure("Malformed request body"));
    }

    /**
     * A path variable couldn't be converted to its declared type - e.g.
     * {@code GET /api/blog/abc} where {@code blogId} is a {@code Long} ->
     * 400. (Phase 5 addition: same rationale as {@link #handleMissingPart}.)
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch on parameter '{}': {}", ex.getName(), ex.getValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("Invalid value for '" + ex.getName() + "'"));
    }

    /**
     * No controller mapped for the requested path -> 404, in the same JSON
     * envelope as every other error instead of Spring Boot's default
     * whitelabel error page. Only takes effect because
     * {@code spring.mvc.throw-exception-if-no-handler-found=true} and
     * {@code spring.web.resources.add-mappings=false} are set in
     * {@code application.yml} (Phase 5 addition).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("No handler for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure("Resource not found"));
    }

    /**
     * Author-mismatch on delete/toggle-publish -> 403. Replaces
     * {@code if (blog.author.toString() !== req.user.userId) { return res.json({success:false, message:"Unauthorized"}) }}.
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedAction(UnauthorizedActionException ex) {
        log.warn("Unauthorized action attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(ex.getMessage()));
    }

    /**
     * A database constraint was violated at the moment of writing - most
     * plausibly a race condition on the unique {@code email} index (two
     * concurrent {@code /register} requests both pass the
     * {@code existsByEmail} pre-check before either has committed) -> 400.
     * {@code AuthService}'s existence check narrows this to a rare edge
     * case rather than the common path, but the constraint itself is the
     * real guarantee, so a request that hits it must still surface a clean
     * client error instead of falling through to a raw 500.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure("The request could not be completed due to a data conflict."));
    }

    /** ImageKit or Gemini call failed -> 500, with a clearer message than a raw stack trace would give. */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse> handleExternalService(ExternalServiceException ex) {
        log.error("External service call failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(ex.getMessage()));
    }

    /**
     * Catch-all fallback -> 500, replacing the generic {@code catch(error)}
     * in every Express controller.
     * <p>
     * Phase 5 bug fix: this used to return {@code ex.getMessage()} directly
     * to the client, which for an unanticipated exception (a raw SQL
     * error, a null pointer, etc.) can leak internal implementation detail
     * to an API consumer. The full exception is now logged server-side at
     * {@code ERROR} with its stack trace, but the client only ever sees a
     * generic, safe message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("An unexpected error occurred. Please try again later."));
    }
}
