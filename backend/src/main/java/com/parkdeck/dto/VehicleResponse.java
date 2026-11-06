package com.parkdeck.dto;

public record VehicleResponse(
        Long id,
        String vehicleType,
        String registrationNumber,
        String nickname
) {
}
