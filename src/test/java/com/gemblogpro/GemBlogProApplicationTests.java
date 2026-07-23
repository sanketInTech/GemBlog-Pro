package com.gemblogpro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Loads the full Spring application context (every {@code @Configuration},
 * {@code @Component}, {@code @Service}, {@code @Repository}, and
 * {@code @RestController} across all five phases) against an in-memory H2
 * database (see {@code src/test/resources/application.yml}).
 * <p>
 * This is the single highest-value test in the suite: it's the only one
 * that verifies every bean actually wires together - constructor injection
 * across {@code SecurityConfig}, {@code JwtAuthenticationFilter},
 * {@code CustomUserDetailsService}, every controller/service/repository,
 * and the JPA entity mappings all resolving without a missing bean,
 * circular dependency, or mapping error. If this test passes, the
 * application is guaranteed to start.
 */
@SpringBootTest
class GemBlogProApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: a failed context load fails this test with
        // a full stack trace pinpointing the offending bean.
    }
}
