package com.parkdeck.dto;

import com.parkdeck.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long parkingSpaceId,
        String spaceTitle,
        String spaceCity,
        String spaceAddress,
        String imageUrl,
        String driverName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        long durationMinutes,
        BigDecimal hourlyRateSnapshot,
        BigDecimal serviceFee,
        BigDecimal totalPrice,
        BookingStatus status,
        Instant createdAt
) {
}
