package com.parkdeck.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Partial hours are billed proportionally. The duration in minutes is divided by 60,
 * rounded half-up to 4 decimal places, then multiplied by the hourly rate and rounded
 * half-up to 2 decimal places. A 5% service fee is calculated on that subtotal and
 * also rounded half-up to 2 decimal places. This is a reservation total, not a payment.
 */
public final class Pricing {

    public static final BigDecimal SERVICE_FEE_RATE = new BigDecimal("0.05");

    private Pricing() {
    }

    public record Quote(BigDecimal hours, BigDecimal subtotal, BigDecimal serviceFee, BigDecimal total) {
    }

    public static Quote quote(BigDecimal hourlyRate, LocalDateTime start, LocalDateTime end) {
        if (hourlyRate == null || hourlyRate.signum() <= 0) {
            throw new IllegalArgumentException("Hourly price must be positive");
        }
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes <= 0) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal subtotal = hourlyRate.multiply(hours).setScale(2, RoundingMode.HALF_UP);
        BigDecimal serviceFee = subtotal.multiply(SERVICE_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        return new Quote(hours, subtotal, serviceFee, subtotal.add(serviceFee));
    }
}
