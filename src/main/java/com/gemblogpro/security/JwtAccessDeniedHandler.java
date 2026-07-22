package com.gemblogpro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemblogpro.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Invoked when an authenticated request is correctly identified but denied
 * for authorization reasons (HTTP 403). The current app has only one class
 * of authenticated user, so this path isn't exercised by anything migrated
 * so far - it's wired up now for completeness and consistency with the
 * approved standard-HTTP-status-code set (200/201/400/401/403/404/500), and
 * to keep every security-layer error response in the same JSON envelope
 * shape as the rest of the API rather than falling back to Spring
 * Security's default HTML error page.
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                        HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.failure("Access denied")));
    }
}
