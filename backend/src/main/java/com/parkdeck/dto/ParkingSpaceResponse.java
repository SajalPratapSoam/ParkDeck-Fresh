package com.parkdeck.dto;

import com.parkdeck.entity.ParkingType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;

public record ParkingSpaceResponse(
        Long id,
        String title,
        String description,
        String address,
        String city,
        ParkingType parkingType,
        BigDecimal hourlyPrice,
        String imageUrl,
        LocalTime availableFrom,
        LocalTime availableUntil,
        boolean active,
        Long hostId,
        String hostName,
        Instant createdAt
) {
}
