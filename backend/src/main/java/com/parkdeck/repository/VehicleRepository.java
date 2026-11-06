package com.parkdeck.repository;

import com.parkdeck.entity.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerIdOrderByIdDesc(Long ownerId);

    Optional<Vehicle> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndRegistrationNumberIgnoreCase(Long ownerId, String registrationNumber);
}
