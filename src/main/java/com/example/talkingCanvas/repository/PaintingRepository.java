package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.Painting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Painting entity
 */
@Repository
public interface PaintingRepository extends JpaRepository<Painting, Long>, JpaSpecificationExecutor<Painting> {

    Page<Painting> findByIsAvailable(Boolean isAvailable, Pageable pageable);

    Page<Painting> findByAdminRecommendationAndIsAvailable(Boolean adminRecommendation, Boolean isAvailable, Pageable pageable);

    @Query("SELECT p FROM Painting p WHERE p.isAvailable = true AND " +
           "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.artistName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.medium) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Painting> searchPaintings(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Painting p WHERE p.isAvailable = true ORDER BY p.viewCount DESC")
    List<Painting> findMostViewedPaintings(Pageable pageable);

    @Query("SELECT p FROM Painting p WHERE p.isAvailable = true ORDER BY p.purchaseCount DESC")
    List<Painting> findBestSellingPaintings(Pageable pageable);

    Long countByIsAvailable(Boolean isAvailable);

    @Query("SELECT p FROM Painting p WHERE p.isAvailable = true AND p.price BETWEEN :minPrice AND :maxPrice")
    Page<Painting> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                     @Param("maxPrice") BigDecimal maxPrice, 
                                     Pageable pageable);
}
