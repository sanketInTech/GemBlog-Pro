package com.gemblogpro.exception;

/**
 * Thrown when a call to an external provider (ImageKit or Gemini) fails.
 * In the Express app these failures were never distinguished from any other
 * error - they fell into the same generic {@code catch(error){ res.json({success:false, message:error.message}) }}
 * block as everything else. This dedicated exception type gives them a
 * distinct, consistent {@code 500} handling path while keeping the same
 * {@code {success:false, message}} response shape.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }
}
