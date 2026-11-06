package com.parkdeck;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.parkdeck.service.Pricing;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PricingTest {

    @Test
    void billsPartialHoursProportionallyAndAddsFivePercentFee() {
        LocalDateTime start = LocalDateTime.of(2026, 11, 8, 10, 0);
        Pricing.Quote ninetyMinutes = Pricing.quote(new BigDecimal("80.00"), start, start.plusMinutes(90));
        assertEquals(new BigDecimal("1.5000"), ninetyMinutes.hours());
        assertEquals(new BigDecimal("120.00"), ninetyMinutes.subtotal());
        assertEquals(new BigDecimal("6.00"), ninetyMinutes.serviceFee());
        assertEquals(new BigDecimal("126.00"), ninetyMinutes.total());

        Pricing.Quote fortyMinutes = Pricing.quote(new BigDecimal("55.00"), start, start.plusMinutes(40));
        assertEquals(new BigDecimal("0.6667"), fortyMinutes.hours());
        assertEquals(new BigDecimal("36.67"), fortyMinutes.subtotal());
        assertEquals(new BigDecimal("1.83"), fortyMinutes.serviceFee());
        assertEquals(new BigDecimal("38.50"), fortyMinutes.total());
    }
}
