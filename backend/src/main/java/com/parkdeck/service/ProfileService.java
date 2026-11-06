package com.parkdeck.service;

import com.parkdeck.dto.UpdateProfileRequest;
import com.parkdeck.dto.UserResponse;
import com.parkdeck.entity.User;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.security.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse get() {
        return AuthService.toResponse(current());
    }

    @Transactional
    public UserResponse update(UpdateProfileRequest request) {
        User user = current();
        user.setFullName(request.fullName().trim());
        user.setPhone(AuthService.blankToNull(request.phone()));
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw ApiException.badRequest("Enter your current password to set a new one");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                throw ApiException.badRequest("Current password is incorrect");
            }
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return AuthService.toResponse(user);
    }

    private User current() {
        User session = SecurityUtils.requireUser();
        return users.findById(session.getId()).orElseThrow(() -> ApiException.unauthorized("Authentication required"));
    }
}
