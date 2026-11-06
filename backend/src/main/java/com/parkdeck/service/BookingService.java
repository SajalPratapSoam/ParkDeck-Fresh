package com.parkdeck.service;

import com.parkdeck.dto.BookingRequest;
import com.parkdeck.dto.BookingResponse;
import com.parkdeck.entity.Booking;
import com.parkdeck.entity.BookingStatus;
import com.parkdeck.entity.ParkingSpace;
import com.parkdeck.entity.Role;
import com.parkdeck.entity.User;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.BookingRepository;
import com.parkdeck.repository.ParkingSpaceRepository;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.security.SecurityUtils;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    static final long MAX_MINUTES = 7L * 24L * 60L;

    private final BookingRepository bookings;
    private final ParkingSpaceRepository spaces;
    private final UserRepository users;

    public BookingService(BookingRepository bookings, ParkingSpaceRepository spaces, UserRepository users) {
        this.bookings = bookings;
        this.spaces = spaces;
        this.users = users;
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        User driver = requireDriver();
        if (request.endDateTime().isBefore(request.startDateTime()) || request.endDateTime().equals(request.startDateTime())) {
            throw ApiException.badRequest("End time must be after the start time");
        }
        LocalDateTime now = LocalDateTime.now(ZONE);
        if (request.startDateTime().isBefore(now)) {
            throw ApiException.badRequest("Bookings cannot start in the past");
        }
        long minutes = Duration.between(request.startDateTime(), request.endDateTime()).toMinutes();
        if (minutes > MAX_MINUTES) {
            throw ApiException.badRequest("A booking cannot be longer than 7 days");
        }

        ParkingSpace space = spaces.lockById(request.parkingSpaceId())
                .orElseThrow(() -> ApiException.notFound("Parking space not found"));
        if (!space.isActive()) {
            throw ApiException.badRequest("This parking space is not available");
        }
        validateAvailabilityWindow(space, request.startDateTime(), request.endDateTime());
        long overlaps = bookings.countOverlappingConfirmed(space.getId(), request.startDateTime(), request.endDateTime());
        if (overlaps > 0) {
            throw ApiException.conflict("That time is already booked. Choose a different window.");
        }

        Pricing.Quote quote = Pricing.quote(space.getHourlyPrice(), request.startDateTime(), request.endDateTime());
        Booking booking = new Booking();
        booking.setParkingSpace(space);
        booking.setDriver(driver);
        booking.setStartDateTime(request.startDateTime());
        booking.setEndDateTime(request.endDateTime());
        booking.setHourlyRateSnapshot(space.getHourlyPrice());
        booking.setServiceFee(quote.serviceFee());
        booking.setTotalPrice(quote.total());
        booking.setStatus(BookingStatus.CONFIRMED);
        return toResponse(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> mine() {
        User user = currentUser();
        return bookings.findForDriver(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(Long id) {
        User user = currentUser();
        Booking booking = bookings.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (!canView(booking, user)) {
            throw ApiException.notFound("Booking not found");
        }
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        User user = currentUser();
        Booking booking = bookings.findById(id).orElseThrow(() -> ApiException.notFound("Booking not found"));
        if (!booking.getDriver().getId().equals(user.getId())) {
            throw ApiException.notFound("Booking not found");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw ApiException.badRequest("This booking is already cancelled");
        }
        if (!booking.getEndDateTime().isAfter(LocalDateTime.now(ZONE))) {
            throw ApiException.badRequest("This booking has already ended and can no longer be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> forHost() {
        User host = currentUser();
        if (host.getRole() != Role.HOST) {
            throw ApiException.forbidden("Only hosts can view listing bookings");
        }
        return bookings.findForHost(host.getId()).stream().map(this::toResponse).toList();
    }

    static void validateAvailabilityWindow(ParkingSpace space, LocalDateTime start, LocalDateTime end) {
        LocalTime open = space.getAvailableFrom();
        LocalTime close = space.getAvailableUntil();
        boolean allDay = open.equals(LocalTime.MIDNIGHT) && !close.isBefore(LocalTime.of(23, 59));
        if (!allDay && !start.toLocalDate().equals(end.toLocalDate())) {
            throw ApiException.badRequest("This space can only be booked within a single day inside its available hours");
        }
        if (start.toLocalTime().isBefore(open) || end.toLocalTime().isAfter(close)) {
            throw ApiException.badRequest("The requested times are outside this space's available hours ("
                    + open + " to " + close + ")");
        }
    }

    private boolean canView(Booking booking, User user) {
        return booking.getDriver().getId().equals(user.getId())
                || booking.getParkingSpace().getHost().getId().equals(user.getId());
    }

    private User requireDriver() {
        User user = currentUser();
        if (user.getRole() != Role.DRIVER) {
            throw ApiException.forbidden("Only driver accounts can create bookings");
        }
        return user;
    }

    private User currentUser() {
        User session = SecurityUtils.requireUser();
        return users.findById(session.getId()).orElseThrow(() -> ApiException.unauthorized("Authentication required"));
    }

    BookingResponse toResponse(Booking booking) {
        ParkingSpace space = booking.getParkingSpace();
        long minutes = Duration.between(booking.getStartDateTime(), booking.getEndDateTime()).toMinutes();
        return new BookingResponse(
                booking.getId(),
                space.getId(),
                space.getTitle(),
                space.getCity(),
                space.getAddress(),
                space.getImageUrl(),
                booking.getDriver().getFullName(),
                booking.getStartDateTime(),
                booking.getEndDateTime(),
                minutes,
                booking.getHourlyRateSnapshot(),
                booking.getServiceFee(),
                booking.getTotalPrice(),
                booking.getStatus(),
                booking.getCreatedAt());
    }
}
