package com.gemblogpro.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gemblogpro.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Invoked by Spring Security whenever an unauthenticated request is denied
 * access to a protected endpoint. Together with {@link JwtAuthenticationFilter},
 * this reproduces {@code middleware/auth.js}'s two distinct JSON error
 * bodies:
 * <pre>
 *   {success:false, message:"No token provided"}
 *   {success:false, message:"Invalid token"}
 * </pre>
 * The one deliberate contract change (approved as part of the architecture
 * revision): the original always responded with HTTP 200 for these
 * failures; this now responds with the standard {@code 401 Unauthorized}
 * status code, with the same JSON body shape.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                          HttpServletResponse response,
                          AuthenticationException authException) throws IOException {

        String message = (String) request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE);
        if (message == null) {
            message = "No token provided";
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.failure(message)));
    }
}
