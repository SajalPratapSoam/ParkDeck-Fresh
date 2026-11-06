package com.parkdeck.dto;

import com.parkdeck.entity.Role;
import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        String phone,
        Instant createdAt
) {
}
