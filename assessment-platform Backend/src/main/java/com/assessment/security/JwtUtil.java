package com.assessment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for creating and validating JSON Web Tokens.
 *
 * Uses JJWT 0.12.x API with HS256 (HMAC-SHA256) signing.
 *
 * The JWT payload contains:
 *   - sub   : username
 *   - role  : STUDENT or TEACHER (used for authorization)
 *   - iat   : issued-at timestamp
 *   - exp   : expiration timestamp
 *
 * SECURITY: Tokens are never logged. The secret key is read from an
 * environment variable (JWT_SECRET), never hardcoded.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a signed JWT for the given username and role.
     *
     * @param username the authenticated user's username
     * @param role     the user's role (STUDENT or TEACHER)
     * @return a compact, URL-safe JWT string
     */
    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .claim(CLAIM_ROLE, role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the username (subject) from a token.
     *
     * @throws JwtException if the token is invalid or expired
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Extracts the role claim from a token.
     *
     * @throws JwtException if the token is invalid or expired
     */
    public String extractRole(String token) {
        return parseClaims(token).get(CLAIM_ROLE, String.class);
    }

    /**
     * Validates that the token is well-formed, signed correctly, and not expired.
     *
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses and returns the claims from a token.
     * Throws a JwtException if the token is invalid or expired.
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
