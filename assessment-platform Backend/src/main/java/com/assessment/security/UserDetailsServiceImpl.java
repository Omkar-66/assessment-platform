package com.assessment.security;

import com.assessment.entity.User;
import com.assessment.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security hook that loads user details from the database by username.
 *
 * Spring Security calls this during authentication. We look up the user,
 * then return a UserDetails object that includes:
 *   - username
 *   - hashed password (Spring Security compares this with the provided password)
 *   - granted authorities (e.g. ROLE_STUDENT or ROLE_TEACHER)
 *
 * The "ROLE_" prefix is a Spring Security convention required for
 * hasRole() expressions to work correctly.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        // Spring Security expects authorities in the form "ROLE_STUDENT" / "ROLE_TEACHER"
        String authority = "ROLE_" + user.getRole().name();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(authority))
        );
    }
}
