package com.assessment.controller;

import com.assessment.dto.AuthResponse;
import com.assessment.dto.LoginRequest;
import com.assessment.dto.RegisterRequest;
import com.assessment.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles public authentication endpoints.
 *
 * POST /api/auth/register  → Student self-registration
 * POST /api/auth/login     → Login for students and teachers
 *
 * These endpoints are open (no JWT required).
 * All other /api/** endpoints require a valid JWT.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new student account.
     *
     * Returns HTTP 201 Created with a JWT so the student is immediately
     * logged in after registration.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a student or teacher and returns a JWT.
     *
     * Returns HTTP 200 OK with the JWT and user info.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
