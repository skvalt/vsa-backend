package com.vsa.controller;

import com.vsa.model.User;
import com.vsa.model.dto.LoginRequest;
import com.vsa.model.dto.RegisterRequest;
import com.vsa.model.response.ApiResponse;
import com.vsa.model.response.AuthResponse;
import com.vsa.repository.UserRepository;
import com.vsa.service.AuthService;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

//Authentication controller
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        try {
            // 1) Create user
            authService.register(req);

            // 2) Auto-login user and return token + profile
            LoginRequest loginReq = new LoginRequest();
            loginReq.setUsername(req.getUsername());
            loginReq.setPassword(req.getPassword());

            AuthResponse resp = authService.login(loginReq);

            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, ex.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            AuthResponse resp = authService.login(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @GetMapping("/me")
public ResponseEntity<?> me(Authentication auth) {
    if (auth == null) return ResponseEntity.status(401).body(Map.of("error", "unauthenticated"));
    String username = auth.getName();
    // Assuming AuthService or UserRepository can fetch user by username
    var user = authService.getProfileByUsername(username); // implement this
    if (user == null) return ResponseEntity.status(404).body(Map.of("error", "user not found"));
    return ResponseEntity.ok(user);
}
}
