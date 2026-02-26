package com.example.restauth.controller;

import com.example.restauth.dto.*;
import com.example.restauth.entity.User;
import com.example.restauth.entity.Role;
import com.example.restauth.repository.UserRepository;
import com.example.restauth.security.JwtUtil;
import com.example.restauth.service.CustomUserDetailsService;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new InputErrorResponse("Error","Username already exists"));
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new InputErrorResponse("Error","Email already in use"));
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)  // default role
                .build();

        userRepository.save(user);

        return ResponseEntity.ok(new RegisterResponse("success", "User registered successfully") );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    request.getUsername());

            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse("success", token));

        } catch (BadCredentialsException ex) {
            // Return JSON error message
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new InputErrorResponse("Error", "Invalid username or password"));
        }
    }
}