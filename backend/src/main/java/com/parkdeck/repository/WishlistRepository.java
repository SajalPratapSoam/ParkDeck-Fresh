package com.parkdeck.repository;

import com.parkdeck.entity.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    @Query("""
            select w from WishlistItem w
            join fetch w.parkingSpace s
            join fetch s.host
            where w.user.id = :userId
            order by w.createdAt desc
            """)
    List<WishlistItem> findForUser(@Param("userId") Long userId);

    Optional<WishlistItem> findByUserIdAndParkingSpaceId(Long userId, Long spaceId);

    boolean existsByUserIdAndParkingSpaceId(Long userId, Long spaceId);
}
