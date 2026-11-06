package com.parkdeck.service;

import com.parkdeck.dto.VehicleRequest;
import com.parkdeck.dto.VehicleResponse;
import com.parkdeck.entity.User;
import com.parkdeck.entity.Vehicle;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.repository.VehicleRepository;
import com.parkdeck.security.SecurityUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {

    private final VehicleRepository vehicles;
    private final UserRepository users;

    public VehicleService(VehicleRepository vehicles, UserRepository users) {
        this.vehicles = vehicles;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> mine() {
        return vehicles.findByOwnerIdOrderByIdDesc(current().getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public VehicleResponse create(VehicleRequest request) {
        User owner = current();
        String registration = request.registrationNumber().replaceAll("\\s+", "").toUpperCase();
        if (vehicles.existsByOwnerIdAndRegistrationNumberIgnoreCase(owner.getId(), registration)) {
            throw ApiException.conflict("You already saved a vehicle with that registration number");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        vehicle.setVehicleType(request.vehicleType().trim());
        vehicle.setRegistrationNumber(registration);
        vehicle.setNickname(AuthService.blankToNull(request.nickname()));
        return toResponse(vehicles.save(vehicle));
    }

    @Transactional
    public void delete(Long id) {
        User owner = current();
        Vehicle vehicle = vehicles.findByIdAndOwnerId(id, owner.getId())
                .orElseThrow(() -> ApiException.notFound("Vehicle not found"));
        vehicles.delete(vehicle);
    }

    private User current() {
        User session = SecurityUtils.requireUser();
        return users.findById(session.getId()).orElseThrow(() -> ApiException.unauthorized("Authentication required"));
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getVehicleType(), vehicle.getRegistrationNumber(), vehicle.getNickname());
    }
}
