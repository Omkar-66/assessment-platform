package com.assessment.config;

import com.assessment.security.JwtAuthenticationFilter;
import com.assessment.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Full Spring Security configuration for the Assessment Platform.
 *
 * Key design decisions:
 *
 * 1. STATELESS sessions — no server-side session is created or used.
 *    Authentication is established on each request via the JWT filter.
 *
 * 2. JWT filter runs before UsernamePasswordAuthenticationFilter so that
 *    the SecurityContext is populated before any authorization checks.
 *
 * 3. CSRF is disabled because we use stateless JWT (no cookie-based sessions).
 *
 * 4. Route protection:
 *    - /api/auth/**        → public (login + register)
 *    - /api/teacher/**     → TEACHER only
 *    - /api/student/**     → STUDENT only
 *
 * 5. BCrypt with strength 12 is used for password hashing.
 *
 * 6. CORS is configured to allow the React dev server (and production origin).
 *    allowedOrigins("*") with credentials is intentionally avoided.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOriginsConfig;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          JwtAuthenticationFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                // Teacher-only endpoints
                .requestMatchers("/api/teacher/**").hasRole("TEACHER")
                // Student-only endpoints
                .requestMatchers("/api/student/**").hasRole("STUDENT")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder with strength 12.
     * Higher strength = more secure but slower hashing.
     * 12 is a good balance for production.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Wires our UserDetailsService and PasswordEncoder into Spring Security's
     * DAO authentication provider.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean so AuthService can call
     * authenticate() during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * CORS configuration for the React frontend.
     *
     * Allowed origins are read from application.properties (app.cors.allowed-origins).
     * This can be overridden per environment via the CORS_ALLOWED_ORIGINS env variable.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Parse comma-separated origins from config
        List<String> origins = Arrays.asList(allowedOriginsConfig.split(","));
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
