package com.parkdeck.controller;

import com.parkdeck.dto.PageResponse;
import com.parkdeck.dto.ParkingSpaceRequest;
import com.parkdeck.dto.ParkingSpaceResponse;
import com.parkdeck.entity.ParkingType;
import com.parkdeck.service.ParkingSpaceService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spaces")
public class ParkingSpaceController {

    private final ParkingSpaceService parkingSpaceService;

    public ParkingSpaceController(ParkingSpaceService parkingSpaceService) {
        this.parkingSpaceService = parkingSpaceService;
    }

    @GetMapping
    public PageResponse<ParkingSpaceResponse> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ParkingType parkingType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return parkingSpaceService.search(city, keyword, parkingType, maxPrice, page, size);
    }

    @GetMapping("/{id}")
    public ParkingSpaceResponse get(@PathVariable Long id) {
        return parkingSpaceService.getPublic(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingSpaceResponse create(@Valid @RequestBody ParkingSpaceRequest request) {
        return parkingSpaceService.create(request);
    }

    @PutMapping("/{id}")
    public ParkingSpaceResponse update(@PathVariable Long id, @Valid @RequestBody ParkingSpaceRequest request) {
        return parkingSpaceService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        parkingSpaceService.deactivate(id);
    }
}
