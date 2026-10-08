package com.marketmate.controller;

import com.marketmate.entity.User;
import com.marketmate.repository.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =========================
    // REGISTER USER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody User user) {

        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered");
        }

        // Save user
        User savedUser = userRepository.save(user);

        // Return user details without password
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserResponse.from(savedUser));
    }

    // =========================
    // LOGIN USER
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @Valid @RequestBody LoginRequest loginRequest) {

        User user = userRepository
                .findByEmail(loginRequest.email())
                .orElse(null);

        // Check email and password
        if (user == null
                || !user.getPassword().equals(loginRequest.password())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        // Login successful
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // =========================
    // LOGIN REQUEST
    // =========================
    private record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Enter a valid email")
            String email,

            @NotBlank(message = "Password is required")
            String password) {
    }

    // =========================
    // USER RESPONSE
    // =========================
    private record UserResponse(
            Long id,
            String name,
            String email) {

        private static UserResponse from(User user) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail()
            );
        }
    }
}