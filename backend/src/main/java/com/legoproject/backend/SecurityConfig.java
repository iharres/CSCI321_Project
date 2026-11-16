package com.legoproject.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();

        /*
         * Allow the local frontend during development.
         */
        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        /*
         * REQUIRED for JSESSIONID cookies.
         */
        configuration.setAllowCredentials(
                true
        );


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

                /*
                 * Enable our CORS configuration.
                 */
                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )

                /*
                 * Leaving CSRF disabled during development.
                 * We can harden this later.
                 */
                .csrf(csrf -> {})

                .authorizeHttpRequests(auth ->
                        auth

                                /*
                                 * Routes available before login.
                                 */
                                .requestMatchers(
                                        "/api/csrf",
                                        "/api/register",
                                        "/api/login",
                                        "/api/session/login",
                                        "/api/session/me",
                                        "/api/session/logout",
                                        "/api/bricklink/**",
                                        "/api/scan-lego"
                                )
                                .permitAll()

                                /*
                                 * Portfolio and /api/sets/**
                                 * now require authentication.
                                 */
                                .anyRequest()
                                .authenticated()
                );


        return http.build();
    }
}