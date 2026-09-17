package com.assessment.security;

import com.assessment.entity.User;
import com.assessment.exception.ResourceNotFoundException;
import com.assessment.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility to retrieve the currently authenticated user from the SecurityContext.
 *
 * The JWT filter populates the SecurityContext on every authenticated request.
 * Services call getCurrentUser() to get the User entity for the request.
 *
 * This is intentionally a Spring-managed @Component so that it can be
 * injected into services via constructor injection (easily testable with mocks).
 */
@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the User entity for the currently authenticated user.
     *
     * @throws ResourceNotFoundException if the user from the token is not in the DB
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + username));
    }

    /**
     * Returns the username of the currently authenticated user.
     */
    public String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
