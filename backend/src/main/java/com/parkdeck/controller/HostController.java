package com.parkdeck.controller;

import com.parkdeck.dto.BookingResponse;
import com.parkdeck.dto.HostSummaryResponse;
import com.parkdeck.dto.ParkingSpaceResponse;
import com.parkdeck.service.BookingService;
import com.parkdeck.service.HostDashboardService;
import com.parkdeck.service.ParkingSpaceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/host")
public class HostController {

    private final HostDashboardService hostDashboardService;
    private final ParkingSpaceService parkingSpaceService;
    private final BookingService bookingService;

    public HostController(
            HostDashboardService hostDashboardService,
            ParkingSpaceService parkingSpaceService,
            BookingService bookingService) {
        this.hostDashboardService = hostDashboardService;
        this.parkingSpaceService = parkingSpaceService;
        this.bookingService = bookingService;
    }

    @GetMapping("/summary")
    public HostSummaryResponse summary() {
        return hostDashboardService.summary();
    }

    @GetMapping("/spaces")
    public List<ParkingSpaceResponse> spaces() {
        return parkingSpaceService.mine();
    }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings() {
        return bookingService.forHost();
    }
}
