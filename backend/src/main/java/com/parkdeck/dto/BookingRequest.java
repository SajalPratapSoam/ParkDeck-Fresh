package com.parkdeck.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record BookingRequest(
        @NotNull(message = "Choose a parking space") Long parkingSpaceId,
        @NotNull(message = "Start time is required") LocalDateTime startDateTime,
        @NotNull(message = "End time is required") LocalDateTime endDateTime
) {
}
