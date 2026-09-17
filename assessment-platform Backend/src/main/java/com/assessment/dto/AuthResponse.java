package com.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response body returned after a successful login or registration.
 *
 * Contains the JWT the client must include in subsequent requests:
 *   Authorization: Bearer <token>
 *
 * The password is NEVER included in any response.
 */
@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String role;

    public AuthResponse(String token, Long userId, String username, String email, String role) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}
