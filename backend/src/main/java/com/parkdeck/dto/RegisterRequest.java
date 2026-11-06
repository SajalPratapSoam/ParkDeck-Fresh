package com.parkdeck.dto;

import com.parkdeck.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must be 120 characters or fewer")
        String fullName,
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email")
        @Size(max = 180)
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
        String password,
        @NotNull(message = "Choose DRIVER or HOST")
        Role role,
        @Size(max = 24, message = "Phone must be 24 characters or fewer")
        String phone
) {
}
