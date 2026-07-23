package com.gemblogpro.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pure unit test - no Spring context - for the core JWT round-trip that
 * every authenticated request in the app depends on. This is deliberately
 * the fastest and most isolated test in the suite: if it fails, the bug is
 * unambiguously in token generation/parsing and not in any surrounding
 * wiring.
 */
class JwtTokenProviderTest {

    // 256-bit (32+ byte) test secret - satisfies JwtTokenProvider's HS256
    // minimum key length check. Never used outside this test.
    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-only-32bytes+";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(TEST_SECRET, 7);
    }

    @Test
    void generateToken_thenParseClaims_roundTripsUserIdAndEmail() {
        String token = jwtTokenProvider.generateToken(42L, "admin@example.com");

        assertThat(token).isNotBlank();

        Claims claims = jwtTokenProvider.parseClaims(token);

        assertThat(jwtTokenProvider.getUserId(claims)).isEqualTo(42L);
        assertThat(jwtTokenProvider.getEmail(claims)).isEqualTo("admin@example.com");
    }

    @Test
    void parseClaims_rejectsTokenSignedWithDifferentSecret() {
        JwtTokenProvider otherProvider = new JwtTokenProvider("a-completely-different-32-byte-secret!!", 7);
        String token = otherProvider.generateToken(1L, "someone@example.com");

        assertThatThrownBy(() -> jwtTokenProvider.parseClaims(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseClaims_rejectsMalformedToken() {
        assertThatThrownBy(() -> jwtTokenProvider.parseClaims("not-a-real-jwt"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void constructor_rejectsBlankSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("", 7));
    }

    @Test
    void constructor_rejectsSecretShorterThan32Bytes() {
        assertThrows(IllegalStateException.class, () -> new JwtTokenProvider("too-short", 7));
    }
}
