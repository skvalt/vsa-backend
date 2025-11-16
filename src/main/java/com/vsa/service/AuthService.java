package com.vsa.service;

import com.vsa.model.User;
import com.vsa.model.dto.LoginRequest;
import com.vsa.model.dto.RegisterRequest;
import com.vsa.model.response.AuthResponse;
import com.vsa.repository.UserRepository;
import com.vsa.security.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

//Handles registration and login logic, including BCrypt hashing and JWT generation.

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public void register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .username(req.getUsername().trim())
                .email(req.getEmail().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role("USER")
                .build();

        userRepository.save(user);
    }
    public User getProfileByUsername(String username) {
    return userRepository.findByUsername(username).orElse(null);
}


    public AuthResponse login(LoginRequest req) {

        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtProvider.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(token);
    }
}
