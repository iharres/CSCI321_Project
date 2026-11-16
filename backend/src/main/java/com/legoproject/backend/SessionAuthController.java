package com.legoproject.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionAuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();


    public SessionAuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        User user =
                userRepository
                        .findByUsername(loginRequest.username())
                        .orElse(null);


        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid username or password"
                    ));
        }


        if (!passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid username or password"
                    ));
        }


        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        user.getUsername(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_USER"
                                )
                        )
                );


        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);


        /*
         * Protect against session fixation if a session
         * already existed before login.
         */
        HttpSession existingSession =
                request.getSession(false);

        if (existingSession != null) {
            request.changeSessionId();
        }


        /*
         * Save authenticated user into the HTTP session.
         */
        securityContextRepository.saveContext(
                context,
                request,
                response
        );


        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "username", user.getUsername()
                )
        );
    }


    // =====================================================
    // CURRENT USER
    // =====================================================

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(
            Authentication authentication) {

        if (
                authentication == null ||
                        !authentication.isAuthenticated()
        ) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }


        User user =
                userRepository
                        .findByUsername(
                                authentication.getName()
                        )
                        .orElse(null);


        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }


        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "username", user.getUsername()
                )
        );
    }


    // =====================================================
    // LOGOUT
    // =====================================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        if (session != null) {
            session.invalidate();
        }


        SecurityContextHolder.clearContext();


        return ResponseEntity
                .noContent()
                .build();
    }


    // =====================================================
    // REQUEST BODY
    // =====================================================

    public record LoginRequest(
            String username,
            String password) {
    }
}