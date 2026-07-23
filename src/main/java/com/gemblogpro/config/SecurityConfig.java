package com.gemblogpro.config;

import com.gemblogpro.security.JwtAccessDeniedHandler;
import com.gemblogpro.security.JwtAuthenticationEntryPoint;
import com.gemblogpro.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security wiring. The Express app had no formal security
 * framework - just the hand-rolled {@code middleware/auth.js} applied
 * per-route in {@code adminRoutes.js} / {@code blogRoutes.js}. This class
 * is the Spring Boot equivalent of that routing decision (which endpoints
 * get {@code auth} vs which don't), made explicit and centralized.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                           JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    /**
     * BCrypt with strength 10, matching {@code bcrypt.hash(password, 10)} in
     * the original {@code adminController.js}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Exposed so {@code AuthService} can authenticate email/password pairs
     * via {@code AuthenticationManager.authenticate(...)} rather than
     * hand-rolling a {@code bcrypt.compare} call - Spring Security's
     * standard login path, backed automatically by the single
     * {@code CustomUserDetailsService} + {@code PasswordEncoder} beans in
     * this context.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Public admin-auth endpoints - the routes declared *without*
                        // the `auth` middleware in adminRoutes.js.
                        .requestMatchers(HttpMethod.POST, "/api/admin/register", "/api/admin/login", "/api/admin/auth")
                        .permitAll()
                        // Public blog-facing endpoints from blogRoutes.js.
                        .requestMatchers(HttpMethod.GET, "/api/blog/all", "/api/blog/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/blog/add-comment", "/api/blog/comments").permitAll()
                        // Container/platform health check (Docker HEALTHCHECK, Render
                        // health check) - must be reachable without a JWT.
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        // Every other /api/** route requires a valid JWT - this is the
                        // equivalent of every route in adminRoutes.js / blogRoutes.js
                        // that has the `auth` middleware applied. This also covers every
                        // /api/admin/** management endpoint and /api/blog/add,
                        // /delete, /toggle-publish, /generate from Phase 4.
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security filter chain configured: stateless JWT auth, public admin auth + public blog read/comment endpoints");
        return http.build();
    }

    /**
     * Mirrors the current {@code app.use(cors())} in {@code server.js},
     * which allows all origins/methods/headers with no restriction.
     * Tightening this to a specific frontend origin is a policy decision
     * left for you to make later, not changed here.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
