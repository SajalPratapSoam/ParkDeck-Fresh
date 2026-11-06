package com.parkdeck.repository;

import com.parkdeck.entity.Booking;
import com.parkdeck.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select b from Booking b
            join fetch b.parkingSpace s
            join fetch b.driver
            where b.driver.id = :driverId
            order by b.createdAt desc
            """)
    List<Booking> findForDriver(@Param("driverId") Long driverId);

    @Query("""
            select b from Booking b
            join fetch b.parkingSpace s
            join fetch s.host
            join fetch b.driver
            where s.host.id = :hostId
            order by b.createdAt desc
            """)
    List<Booking> findForHost(@Param("hostId") Long hostId);

    @Query("""
            select count(b) from Booking b
            where b.parkingSpace.id = :spaceId
              and b.status = com.parkdeck.entity.BookingStatus.CONFIRMED
              and b.startDateTime < :end
              and b.endDateTime > :start
            """)
    long countOverlappingConfirmed(
            @Param("spaceId") Long spaceId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    long countByParkingSpaceHostIdAndStatus(Long hostId, BookingStatus status);

    @Query("""
            select coalesce(sum(b.totalPrice), 0)
            from Booking b
            where b.parkingSpace.host.id = :hostId
              and b.status = com.parkdeck.entity.BookingStatus.CONFIRMED
            """)
    BigDecimal sumConfirmedEarnings(@Param("hostId") Long hostId);
}
