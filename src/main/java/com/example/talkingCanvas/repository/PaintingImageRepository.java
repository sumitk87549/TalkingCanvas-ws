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
}
