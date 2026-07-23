package com.gemblogpro.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Replaces {@code middleware/auth.js}.
 * <p>
 * Preserved from the original middleware:
 * <ul>
 *   <li>Reads the raw token straight from the {@code Authorization} header -
 *       no {@code "Bearer "} prefix - matching {@code AppContext.jsx}'s
 *       {@code axios.defaults.headers.common['Authorization'] = `${token}`}.</li>
 *   <li>Two distinct failure reasons: "No token provided" vs "Invalid token",
 *       preserved via {@link #AUTH_ERROR_ATTRIBUTE} and read by
 *       {@link JwtAuthenticationEntryPoint} when Spring Security ultimately
 *       rejects the request.</li>
 * </ul>
 * One deliberate improvement over the original: the original middleware
 * only checked the JWT signature and never re-verified the user still
 * exists in the database (it just did {@code req.user = decoded}). This
 * filter loads the user via {@link CustomUserDetailsService}, so a token
 * issued to an account that has since been deleted is now correctly
 * rejected instead of silently continuing to work. This does not change
 * the request/response contract - a rejected request still surfaces as the
 * same {@code {success:false, message:"Invalid token"}} shape.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_ERROR_ATTRIBUTE = "jwt_auth_error";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                    CustomUserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader(AUTH_HEADER);

        if (token == null || token.isBlank()) {
            // Mirrors `if(!token){ return res.json({success:false, message:"No token provided"}) }`.
            // Do not reject here directly - endpoints that are permitAll() must
            // still proceed unauthenticated. Protected endpoints will be denied
            // downstream by the authorization rules, at which point
            // JwtAuthenticationEntryPoint reads this attribute.
            log.debug("No Authorization header present for {} {}", request.getMethod(), request.getRequestURI());
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "No token provided");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            String email = jwtTokenProvider.getEmail(claims);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authenticated request for {} {} as email={}", request.getMethod(), request.getRequestURI(), email);
            }
        } catch (Exception ex) {
            // Mirrors the catch block in auth.js: `res.json({success:false, message:"Invalid token"})`.
            // Covers an expired/malformed/tampered token as well as a token
            // whose user no longer exists. The token value itself is never
            // logged - only the failure reason.
            log.warn("Rejected invalid token on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
            SecurityContextHolder.clearContext();
            request.setAttribute(AUTH_ERROR_ATTRIBUTE, "Invalid token");
        }

        filterChain.doFilter(request, response);
    }
}
