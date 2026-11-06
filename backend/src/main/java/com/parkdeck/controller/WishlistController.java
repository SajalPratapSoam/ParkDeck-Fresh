package com.parkdeck.controller;

import com.parkdeck.dto.ParkingSpaceResponse;
import com.parkdeck.service.WishlistService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<ParkingSpaceResponse> mine() {
        return wishlistService.mine();
    }

    @PostMapping("/{spaceId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingSpaceResponse add(@PathVariable Long spaceId) {
        return wishlistService.add(spaceId);
    }

    @DeleteMapping("/{spaceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long spaceId) {
        wishlistService.remove(spaceId);
    }
}
