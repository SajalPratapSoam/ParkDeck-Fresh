package com.parkdeck.service;

import com.parkdeck.dto.ParkingSpaceResponse;
import com.parkdeck.entity.ParkingSpace;
import com.parkdeck.entity.User;
import com.parkdeck.entity.WishlistItem;
import com.parkdeck.exception.ApiException;
import com.parkdeck.repository.ParkingSpaceRepository;
import com.parkdeck.repository.UserRepository;
import com.parkdeck.repository.WishlistRepository;
import com.parkdeck.security.SecurityUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistService {

    private final WishlistRepository wishlist;
    private final ParkingSpaceRepository spaces;
    private final UserRepository users;
    private final ParkingSpaceService parkingSpaceService;

    public WishlistService(
            WishlistRepository wishlist,
            ParkingSpaceRepository spaces,
            UserRepository users,
            ParkingSpaceService parkingSpaceService) {
        this.wishlist = wishlist;
        this.spaces = spaces;
        this.users = users;
        this.parkingSpaceService = parkingSpaceService;
    }

    @Transactional(readOnly = true)
    public List<ParkingSpaceResponse> mine() {
        User user = current();
        return wishlist.findForUser(user.getId()).stream()
                .map(item -> parkingSpaceService.toResponse(item.getParkingSpace()))
                .toList();
    }

    @Transactional
    public ParkingSpaceResponse add(Long spaceId) {
        User user = current();
        if (wishlist.existsByUserIdAndParkingSpaceId(user.getId(), spaceId)) {
            throw ApiException.conflict("This space is already on your wishlist");
        }
        ParkingSpace space = spaces.findById(spaceId).orElseThrow(() -> ApiException.notFound("Parking space not found"));
        if (!space.isActive()) {
            throw ApiException.badRequest("Inactive listings cannot be saved");
        }
        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setParkingSpace(space);
        wishlist.save(item);
        return parkingSpaceService.toResponse(space);
    }

    @Transactional
    public void remove(Long spaceId) {
        User user = current();
        WishlistItem item = wishlist.findByUserIdAndParkingSpaceId(user.getId(), spaceId)
                .orElseThrow(() -> ApiException.notFound("That space is not on your wishlist"));
        wishlist.delete(item);
    }

    private User current() {
        User session = SecurityUtils.requireUser();
        return users.findById(session.getId()).orElseThrow(() -> ApiException.unauthorized("Authentication required"));
    }
}
