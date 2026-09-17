package com.assessment.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Consistent error response body returned for all API errors.
 *
 * Example JSON:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "message": "Assessment not found with id: 5",
 *   "path": "/api/teacher/assessments/5"
 * }
 */
public record ErrorResponse(
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String message,
        String path
) {}
