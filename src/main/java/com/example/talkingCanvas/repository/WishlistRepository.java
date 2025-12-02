package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Wishlist entity
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    /**
     * Find wishlist by user ID
     */
    Optional<Wishlist> findByUserId(Long userId);

    /**
     * Get total count of items in user's wishlist
     */
    @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.wishlist.user.id = :userId")
    Long getTotalItemsCountByUserId(@Param("userId") Long userId);

    /**
     * Check if a painting exists in user's wishlist
     */
    @Query("SELECT CASE WHEN COUNT(wi) > 0 THEN true ELSE false END " +
            "FROM WishlistItem wi WHERE wi.wishlist.user.id = :userId AND wi.painting.id = :paintingId")
    boolean existsByUserIdAndPaintingId(@Param("userId") Long userId, @Param("paintingId") Long paintingId);
}
