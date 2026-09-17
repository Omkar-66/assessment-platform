package com.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a client request is logically invalid.
 *
 * Examples:
 *  - Username or email already taken during registration
 *  - Publishing an assessment that has no questions
 *  - Starting an assessment that is not PUBLISHED
 *
 * Results in HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
