package com.parkdeck.service;

import com.parkdeck.dto.PageResponse;
import com.parkdeck.dto.ParkingSpaceRequest;
import com.parkdeck.dto.ParkingSpaceResponse;
import com.parkdeck.entity.ParkingSpace;
import com.parkdeck.entity.ParkingType;
import com.parkdeck.entity.Role;
import com.parkdeck.entity.User;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.ParkingSpaceRepository;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParkingSpaceService {

    private final ParkingSpaceRepository spaces;
    private final UserRepository users;

    public ParkingSpaceService(ParkingSpaceRepository spaces, UserRepository users) {
        this.spaces = spaces;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public PageResponse<ParkingSpaceResponse> search(String city, String keyword, ParkingType parkingType, BigDecimal maxPrice, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Specification<ParkingSpace> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (hasText(city)) {
                predicates.add(cb.like(cb.lower(root.get("city")), "%" + city.trim().toLowerCase() + "%"));
            }
            if (parkingType != null) {
                predicates.add(cb.equal(root.get("parkingType"), parkingType));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("hourlyPrice"), maxPrice));
            }
            if (hasText(keyword)) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("address")), like),
                        cb.like(cb.lower(root.get("city")), like)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<ParkingSpaceResponse> result = spaces.findAll(spec, PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(this::toResponse);
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public ParkingSpaceResponse getPublic(Long id) {
        ParkingSpace space = spaces.findById(id).orElseThrow(() -> ApiException.notFound("Parking space not found"));
        if (!space.isActive()) {
            User viewer = SecurityUtils.optionalUser();
            if (viewer == null || !space.getHost().getId().equals(viewer.getId())) {
                throw ApiException.notFound("Parking space not found");
            }
        }
        return toResponse(space);
    }

    @Transactional
    public ParkingSpaceResponse create(ParkingSpaceRequest request) {
        User host = requireHost();
        ParkingSpace space = new ParkingSpace();
        space.setHost(host);
        apply(space, request, true);
        return toResponse(spaces.save(space));
    }

    @Transactional
    public ParkingSpaceResponse update(Long id, ParkingSpaceRequest request) {
        User host = requireHost();
        ParkingSpace space = spaces.findById(id).orElseThrow(() -> ApiException.notFound("Parking space not found"));
        if (!space.getHost().getId().equals(host.getId())) {
            throw ApiException.forbidden("You can only edit your own listings");
        }
        apply(space, request, space.isActive());
        return toResponse(space);
    }

    @Transactional
    public void deactivate(Long id) {
        User host = requireHost();
        ParkingSpace space = spaces.findById(id).orElseThrow(() -> ApiException.notFound("Parking space not found"));
        if (!space.getHost().getId().equals(host.getId())) {
            throw ApiException.forbidden("You can only deactivate your own listings");
        }
        space.setActive(false);
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponse> mine() {
        User host = requireHost();
        return spaces.findByHostIdOrderByCreatedAtDesc(host.getId()).stream().map(this::toResponse).toList();
    }

    private User requireHost() {
        User session = SecurityUtils.requireUser();
        User user = users.findById(session.getId()).orElseThrow(() -> ApiException.unauthorized("Authentication required"));
        if (user.getRole() != Role.HOST) {
            throw ApiException.forbidden("Only hosts can manage listings");
        }
        return user;
    }

    private void apply(ParkingSpace space, ParkingSpaceRequest request, boolean activeDefault) {
        validateHours(request.availableFrom(), request.availableUntil());
        validateImage(request.imageUrl());
        if (request.hourlyPrice().signum() <= 0) {
            throw ApiException.badRequest("Hourly price must be positive");
        }
        space.setTitle(request.title().trim());
        space.setDescription(request.description().trim());
        space.setAddress(request.address().trim());
        space.setCity(request.city().trim());
        space.setParkingType(request.parkingType());
        space.setHourlyPrice(request.hourlyPrice());
        space.setImageUrl(request.imageUrl().trim());
        space.setAvailableFrom(request.availableFrom());
        space.setAvailableUntil(request.availableUntil());
        space.setActive(request.active() == null ? activeDefault : request.active());
    }

    static void validateHours(LocalTime from, LocalTime until) {
        if (!until.isAfter(from)) {
            throw ApiException.badRequest("Available until must be later than available from on the same day");
        }
    }

    static void validateImage(String imageUrl) {
        String url = imageUrl.trim().toLowerCase();
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            throw ApiException.badRequest("Image URL must start with http:// or https://");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    ParkingSpaceResponse toResponse(ParkingSpace space) {
        return new ParkingSpaceResponse(
                space.getId(),
                space.getTitle(),
                space.getDescription(),
                space.getAddress(),
                space.getCity(),
                space.getParkingType(),
                space.getHourlyPrice(),
                space.getImageUrl(),
                space.getAvailableFrom(),
                space.getAvailableUntil(),
                space.isActive(),
                space.getHost().getId(),
                space.getHost().getFullName(),
                space.getCreatedAt());
    }
}
