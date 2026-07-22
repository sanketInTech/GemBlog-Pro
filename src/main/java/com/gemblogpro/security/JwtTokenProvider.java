package com.gemblogpro.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Issues and validates JSON Web Tokens.
 * <p>
 * Replaces the {@code jwt.sign(...)} / {@code jwt.verify(...)} calls made
 * with the {@code jsonwebtoken} npm package in
 * {@code adminController.js}'s {@code adminLogin} and
 * {@code middleware/auth.js}.
 * <p>
 * Behavior preserved from the Express app: HMAC-signed token, 7-day
 * expiry (configurable, defaults to 7 to match {@code {expiresIn: '7d'}}
 * exactly), single shared secret from {@code JWT_SECRET}.
 * <p>
 * One internal (non-observable) difference from the original: the Express
 * token carried {@code userId} and {@code email} as two top-level claims.
 * Here, {@code userId} is stored as the JWT {@code subject} (a plain
 * string) and {@code email} as a single custom claim. This avoids a known
 * JJWT footgun where a numeric claim serialized as JSON can deserialize as
 * {@code Integer} instead of {@code Long} and fail a typed
 * {@code claims.get(key, Long.class)} lookup. The frontend never inspects
 * the token's internal claims (it only stores/forwards the opaque token
 * string), so this has no effect on the API contract.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_EMAIL = "email";

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-days:7}") long expirationDays) {

        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is not configured. Set the JWT_SECRET environment variable " +
                    "before starting the application.");
        }

        try {
            this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException ex) {
            throw new IllegalStateException(
                    "JWT_SECRET is too short for HS256. It must be at least 32 bytes " +
                    "(256 bits) once UTF-8 encoded - e.g. generate one with " +
                    "`openssl rand -base64 48`.", ex);
        }

        this.expirationMillis = expirationDays * 24L * 60L * 60L * 1000L;
    }

    /**
     * Generates a signed JWT for a successfully authenticated user.
     * Replaces {@code jwt.sign({userId, email}, JWT_SECRET, {expiresIn:'7d'})}.
     */
    public String generateToken(Long userId, String email) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_EMAIL, email)
                .issuedAt(issuedAt)
                .expiration(expiresAt)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and verifies a token, returning its claims.
     * Replaces {@code jwt.verify(token, JWT_SECRET)}.
     *
     * @throws JwtException if the token is malformed, expired, or the
     *                       signature does not match.
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String getEmail(Claims claims) {
        return claims.get(CLAIM_EMAIL, String.class);
    }
}
