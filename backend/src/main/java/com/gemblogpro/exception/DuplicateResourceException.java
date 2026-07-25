package com.gemblogpro.exception;

/**
 * Thrown when a create operation collides with an existing unique resource.
 * Replaces {@code if(userExists){ return res.json({success:false, message:'Email already registered'}) }}
 * in {@code adminController.js}'s {@code adminRegister}.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
