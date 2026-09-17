package com.assessment.config;

import com.assessment.entity.Role;
import com.assessment.entity.User;
import com.assessment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Creates a default teacher account on application startup if one does not exist.
 *
 * This is useful for:
 *  - Local development (no manual DB setup needed)
 *  - Portfolio demos and interviews
 *
 * IMPORTANT: Change the default password before any public deployment.
 *
 * Default credentials:
 *   username : admin_teacher
 *   password : teacher@123
 *   role     : TEACHER
 *
 * These are logged at INFO level on startup (password is NOT logged).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String DEFAULT_TEACHER_USERNAME = "admin_teacher";
    private static final String DEFAULT_TEACHER_EMAIL    = "teacher@assessment.com";
    private static final String DEFAULT_TEACHER_PASSWORD = "teacher@123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername(DEFAULT_TEACHER_USERNAME)) {
            User teacher = new User(
                    DEFAULT_TEACHER_USERNAME,
                    DEFAULT_TEACHER_EMAIL,
                    passwordEncoder.encode(DEFAULT_TEACHER_PASSWORD),
                    Role.TEACHER
            );
            teacher.setCreatedAt(LocalDateTime.now());
            teacher.setUpdatedAt(LocalDateTime.now());
            userRepository.save(teacher);

            log.info("=================================================");
            log.info("Default teacher account created.");
            log.info("  Username : {}", DEFAULT_TEACHER_USERNAME);
            log.info("  Password : (see DataInitializer.java)");
            log.info("  Role     : TEACHER");
            log.info("  IMPORTANT: Change this password before deployment!");
            log.info("=================================================");
        }
    }
}
