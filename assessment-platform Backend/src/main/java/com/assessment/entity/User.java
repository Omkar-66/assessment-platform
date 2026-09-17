package com.assessment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Represents a registered user.
 *
 * Maps to the `users` table. A user is either a STUDENT or a TEACHER.
 * Teachers create assessments; students attempt them.
 *
 * Relationships owned by other tables (assessments.created_by,
 * assessment_attempts.student_id) are NOT mapped here as @OneToMany
 * to avoid unnecessary eager loading and JSON serialization issues.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /**
     * Stored as a BCrypt hash. Never returned in API responses.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * STUDENT or TEACHER. Stored as the enum string value in MySQL.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at",
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    /**
     * Convenience constructor used during registration.
     */
    public User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
