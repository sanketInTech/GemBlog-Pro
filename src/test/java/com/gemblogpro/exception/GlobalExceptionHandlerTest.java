package com.gemblogpro.exception;

import com.gemblogpro.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the HTTP status code each exception type maps to - this is the
 * single most important contract {@link GlobalExceptionHandler} provides
 * (per the approved architecture revision's standard status code set:
 * 200/201/400/401/403/404/500), so a status-code regression here would
 * otherwise only be caught manually against a live server.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void duplicateResourceException_mapsTo400() {
        ResponseEntity<ApiResponse> response =
                handler.handleDuplicateResource(new DuplicateResourceException("Email already registered"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage()).isEqualTo("Email already registered");
    }

    @Test
    void invalidCredentialsException_mapsTo401() {
        ResponseEntity<ApiResponse> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void badCredentialsException_mapsTo401WithGenericMessage() {
        ResponseEntity<ApiResponse> response =
                handler.handleBadCredentials(new BadCredentialsException("some internal detail"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void resourceNotFoundException_mapsTo404() {
        ResponseEntity<ApiResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("Blog not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void unauthorizedActionException_mapsTo403() {
        ResponseEntity<ApiResponse> response =
                handler.handleUnauthorizedAction(new UnauthorizedActionException("Unauthorized"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void externalServiceException_mapsTo500() {
        ResponseEntity<ApiResponse> response =
                handler.handleExternalService(new ExternalServiceException("Image upload failed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void dataIntegrityViolationException_mapsTo400() {
        ResponseEntity<ApiResponse> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException("constraint violated"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void genericException_mapsTo500AndNeverLeaksRawMessage() {
        ResponseEntity<ApiResponse> response =
                handler.handleGeneric(new RuntimeException("raw SQL error with column names"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("raw SQL error");
    }
}
