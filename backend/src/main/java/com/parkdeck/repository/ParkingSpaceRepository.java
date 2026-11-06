package com.parkdeck.repository;

import com.parkdeck.entity.ParkingSpace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long>, JpaSpecificationExecutor<ParkingSpace> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ParkingSpace p where p.id = :id")
    Optional<ParkingSpace> lockById(@Param("id") Long id);

    List<ParkingSpace> findByHostIdOrderByCreatedAtDesc(Long hostId);

    long countByHostIdAndActive(Long hostId, boolean active);
}
