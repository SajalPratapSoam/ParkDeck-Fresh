package com.parkdeck.service;

import com.parkdeck.dto.AuthResponse;
import com.parkdeck.dto.LoginRequest;
import com.parkdeck.dto.RegisterRequest;
import com.parkdeck.dto.UserResponse;
import com.parkdeck.entity.User;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.security.JwtService;
import com.parkdeck.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("An account with that email already exists");
        }
        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setPhone(blankToNull(request.phone()));
        users.save(user);
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> ApiException.unauthorized("Email or password is incorrect"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Email or password is incorrect");
        }
        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        User session = SecurityUtils.requireUser();
        User user = users.findById(session.getId())
                .orElseThrow(() -> ApiException.unauthorized("Authentication required"));
        return toResponse(user);
    }

    private AuthResponse tokenFor(User user) {
        String token = jwtService.createToken(user.getId(), user.getRole().name());
        return new AuthResponse(token, toResponse(user));
    }

    static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getPhone(),
                user.getCreatedAt());
    }

    static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
