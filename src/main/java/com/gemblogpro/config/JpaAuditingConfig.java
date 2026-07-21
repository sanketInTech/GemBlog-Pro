package com.gemblogpro.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables Spring Data JPA auditing so entity {@code @CreatedDate} /
 * {@code @LastModifiedDate} fields are populated automatically.
 * <p>
 * Replaces Mongoose's {@code { timestamps: true }} schema option, which
 * automatically stamped {@code createdAt} / {@code updatedAt} on every
 * document in {@code models/User.js}, {@code models/Blog.js}, and
 * {@code models/Comments.js}.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
