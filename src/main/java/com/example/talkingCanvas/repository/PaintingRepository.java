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
import java.util.Optional;

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

    // Custom save methods for paintings with images
    @Override
    <S extends Painting> S save(S entity);

    @Override
    <S extends Painting> List<S> saveAll(Iterable<S> entities);

    // Custom find methods for paintings with images
    @Query("SELECT DISTINCT p FROM Painting p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Painting> findByIdWithImages(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Painting p LEFT JOIN FETCH p.images WHERE p.isAvailable = true")
    List<Painting> findAllAvailableWithImages(Pageable pageable);

// Exist methods
boolean existsByTitle(String title);

boolean existsByTitleAndIdNot(String title, Long id);

// Custom save method that ensures images are saved
default Painting saveWithImages(Painting painting) {
    // Validate images have proper painting reference
    if (painting.getImages() != null) {
        painting.getImages().forEach(image -> {
            if (image.getPainting() == null) {
                image.setPainting(painting);
            }
        });
    }

    // Save the painting with cascading to images
    return save(painting);
}

// Batch save with images validation
default List<Painting> saveAllWithImages(List<Painting> paintings) {
    // Ensure all images have proper painting references
    paintings.forEach(painting -> {
        if (painting.getImages() != null) {
            painting.getImages().forEach(image -> {
                if (image.getPainting() == null) {
                    image.setPainting(painting);
                }
            });
        }
    });

    // Save all paintings with cascading to images
    return saveAll(paintings).stream().toList();
}
}
