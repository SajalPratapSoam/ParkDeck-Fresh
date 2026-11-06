package com.parkdeck.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120)
        String fullName,
        @Size(max = 24)
        String phone,
        String currentPassword,
        @Size(min = 8, max = 72, message = "New password must be 8 to 72 characters")
        String newPassword
) {
}
