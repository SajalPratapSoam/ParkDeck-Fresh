package com.parkdeck.service;

import com.parkdeck.dto.HostSummaryResponse;
import com.parkdeck.entity.BookingStatus;
import com.parkdeck.entity.Role;
import com.parkdeck.entity.User;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.BookingRepository;
import com.parkdeck.repository.ParkingSpaceRepository;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.security.SecurityUtils;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HostDashboardService {

    public static final String EARNINGS_NOTE =
            "Earnings are the sum of totalPrice on confirmed bookings only. Cancelled bookings are excluded. These figures are reservation totals, not collected payments.";

    private final ParkingSpaceRepository spaces;
    private final BookingRepository bookings;
    private final UserRepository users;
    private final ParkingSpaceService parkingSpaceService;
    private final BookingService bookingService;

    public HostDashboardService(
            ParkingSpaceRepository spaces,
            BookingRepository bookings,
            UserRepository users,
            ParkingSpaceService parkingSpaceService,
            BookingService bookingService) {
        this.spaces = spaces;
        this.bookings = bookings;
        this.users = users;
        this.parkingSpaceService = parkingSpaceService;
        this.bookingService = bookingService;
    }

    @Transactional(readOnly = true)
    public HostSummaryResponse summary() {
        User host = requireHost();
        BigDecimal earnings = bookings.sumConfirmedEarnings(host.getId());
        if (earnings == null) {
            earnings = BigDecimal.ZERO;
        }
        return new HostSummaryResponse(
                spaces.countByHostIdAndActive(host.getId(), true),
                spaces.countByHostIdAndActive(host.getId(), false),
                bookings.countByParkingSpaceHostIdAndStatus(host.getId(), BookingStatus.CONFIRMED),
                bookings.countByParkingSpaceHostIdAndStatus(host.getId(), BookingStatus.CANCELLED),
                earnings,
                EARNINGS_NOTE,
                parkingSpaceService.mine(),
                bookingService.forHost());
    }

    private User requireHost() {
        User session = SecurityUtils.requireUser();
        User user = users.findById(session.getId()).orElseThrow(() -> ApiException.unauthorized("Authentication required"));
        if (user.getRole() != Role.HOST) {
            throw ApiException.forbidden("Only hosts can view the host dashboard");
        }
        return user;
    }
}
