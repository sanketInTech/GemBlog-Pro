package com.gemblogpro.exception;

import com.gemblogpro.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Bean Validation failures on @Valid request DTOs -> 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(message));
    }

    /** Email already registered -> 400. */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse> handleDuplicateResource(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(ex.getMessage()));
    }

    /** Wrong email/password on login -> 401. */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
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
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure("Invalid credentials"));
    }

    /** Requested entity not found -> 404 (foundation for Phase 4). */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(ex.getMessage()));
    }

    /** Catch-all fallback -> 500, replacing the generic `catch(error)` in every Express controller. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(ex.getMessage()));
    }
}
