package com.parkdeck.dto;

import com.parkdeck.entity.ParkingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalTime;

public record ParkingSpaceRequest(
        @NotBlank @Size(max = 140) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotBlank @Size(max = 255) String address,
        @NotBlank @Size(max = 80) String city,
        @NotNull ParkingType parkingType,
        @NotNull @DecimalMin(value = "0.01", message = "Hourly price must be positive")
        @Digits(integer = 8, fraction = 2) BigDecimal hourlyPrice,
        @NotBlank @Size(max = 500) String imageUrl,
        @NotNull LocalTime availableFrom,
        @NotNull LocalTime availableUntil,
        Boolean active
) {
}
