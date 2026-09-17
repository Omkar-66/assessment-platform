package com.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a user tries to access a resource they are not allowed to.
 *
 * Examples:
 *  - A student tries to submit another student's attempt
 *  - A teacher tries to modify another teacher's assessment
 *
 * Results in HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
