package com.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a student tries to modify or re-submit an attempt
 * that has already been submitted.
 *
 * Results in HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AssessmentAlreadySubmittedException extends RuntimeException {

    public AssessmentAlreadySubmittedException(String message) {
        super(message);
    }
}
