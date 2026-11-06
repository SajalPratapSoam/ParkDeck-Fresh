package com.parkdeck.dto;

import java.math.BigDecimal;
import java.util.List;

public record HostSummaryResponse(
        long activeListings,
        long inactiveListings,
        long confirmedBookings,
        long cancelledBookings,
        BigDecimal earnings,
        String earningsNote,
        List<ParkingSpaceResponse> spaces,
        List<BookingResponse> bookings
) {
}
