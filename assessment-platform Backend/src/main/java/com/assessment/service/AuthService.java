package com.assessment.service;

import com.assessment.dto.AuthResponse;
import com.assessment.dto.LoginRequest;
import com.assessment.dto.RegisterRequest;
import com.assessment.entity.Role;
import com.assessment.entity.User;
import com.assessment.exception.BadRequestException;
import com.assessment.repository.UserRepository;
import com.assessment.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles user registration and authentication.
 *
 * Registration is for students only. Teachers are managed outside this flow.
 *
 * SECURITY:
 *  - Passwords are hashed with BCrypt before being stored.
 *  - Passwords are never logged or returned in any response.
 *  - JWT tokens are generated after successful authentication.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new student.
     *
     * Checks for duplicate username and email before saving.
     * Password is hashed with BCrypt before storage.
     *
     * @param request the registration details
     * @return an AuthResponse containing the JWT and user info
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),  // BCrypt hash
                Role.STUDENT
        );
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("New student registered: {}", savedUser.getUsername());

        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getRole().name());
        return buildAuthResponse(token, savedUser);
    }

    /**
     * Authenticates a user (student or teacher) and returns a JWT.
     *
     * Delegates to Spring Security's AuthenticationManager, which internally:
     *   1. Calls UserDetailsServiceImpl.loadUserByUsername()
     *   2. Verifies the password using BCryptPasswordEncoder
     *   3. Throws BadCredentialsException if authentication fails
     *
     * @param request login credentials
     * @return an AuthResponse containing the JWT and user info
     */
    public AuthResponse login(LoginRequest request) {
        // This line does the full authentication — throws if credentials are wrong
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User not found"));

        log.info("User logged in: {} ({})", username, user.getRole());

        String token = jwtUtil.generateToken(username, user.getRole().name());
        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
