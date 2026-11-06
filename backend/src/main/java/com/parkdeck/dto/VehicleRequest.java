package com.parkdeck.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VehicleRequest(
        @NotBlank(message = "Vehicle type is required")
        @Size(max = 40)
        String vehicleType,
        @NotBlank(message = "Registration number is required")
        @Size(max = 20)
        String registrationNumber,
        @Size(max = 40)
        String nickname
) {
}
