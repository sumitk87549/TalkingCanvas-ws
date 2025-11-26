package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Cart entity
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.items ci WHERE c.user.id = :userId")
    Long getTotalItemsCountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM Cart c JOIN c.items ci WHERE c.user.id = :userId AND ci.painting.id = :paintingId")
    Long getItemCountInCartByUserIdAndPaintingId(@Param("userId") Long userId, @Param("paintingId") Long paintingId);
}
