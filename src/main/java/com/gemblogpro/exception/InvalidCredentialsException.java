package com.gemblogpro.exception;

/**
 * Thrown when login credentials don't match an existing account.
 * Replaces {@code if(!user || !(await bcrypt.compare(...))){ return res.json({success:false, message:'Invalid credentials'}) }}
 * in {@code adminController.js}'s {@code adminLogin}.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
