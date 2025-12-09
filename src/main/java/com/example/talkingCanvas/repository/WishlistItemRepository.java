package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for WishlistItem entity
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    /**
     * Find wishlist item by wishlist ID and painting ID
     */
    Optional<WishlistItem> findByWishlistIdAndPaintingId(Long wishlistId, Long paintingId);
}
