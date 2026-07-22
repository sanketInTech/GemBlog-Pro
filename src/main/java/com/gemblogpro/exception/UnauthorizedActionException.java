package com.gemblogpro.exception;

/**
 * Thrown when an authenticated user attempts an action on a resource they
 * don't own. Replaces the author-mismatch checks in
 * {@code blogController.js}'s {@code deleteBlogByID} and
 * {@code togglePublish}:
 * <pre>
 *   if (blog.author.toString() !== req.user.userId) {
 *     return res.json({success:false, message:"Unauthorized"});
 *   }
 * </pre>
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}
