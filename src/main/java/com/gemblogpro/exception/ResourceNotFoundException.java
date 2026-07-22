package com.gemblogpro.exception;

/**
 * Thrown when a requested entity does not exist. Not raised by anything in
 * Phase 3 (no auth endpoint looks up a resource by ID), but declared and
 * wired into {@link GlobalExceptionHandler} now since it's a foundational,
 * cross-cutting exception type that Phase 4's blog/comment lookups
 * ({@code getBlogByID}, etc.) will throw.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
