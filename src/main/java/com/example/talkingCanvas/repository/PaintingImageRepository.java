package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.PaintingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PaintingImage entity
 */
@Repository
public interface PaintingImageRepository extends JpaRepository<PaintingImage, Long> {

    List<PaintingImage> findByPaintingIdOrderByDisplayOrderAsc(Long paintingId);

    // Batch save methods for multiple images
    default List<PaintingImage> saveImagesForPainting(List<PaintingImage> images) {
        return saveAll(images).stream().toList();
    }

    // Find methods for image management
    List<PaintingImage> findByPaintingId(Long paintingId);

    List<PaintingImage> findByIsPrimaryTrue();

    List<PaintingImage> findByPaintingIdAndIsPrimaryTrue(Long paintingId);

    // Delete methods
    void deleteByPaintingId(Long paintingId);

    void deleteByPaintingIdAndIdNotIn(Long paintingId, List<Long> keepIds);

    // Exist methods
    boolean existsByPaintingIdAndIsPrimaryTrue(Long paintingId);
}
