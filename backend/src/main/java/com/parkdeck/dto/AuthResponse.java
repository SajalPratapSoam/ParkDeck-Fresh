package com.parkdeck.dto;

public record AuthResponse(String token, UserResponse user) {
}
