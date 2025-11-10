package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.PaintingCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for PaintingCategory entity
 */
@Repository
public interface PaintingCategoryRepository extends JpaRepository<PaintingCategory, Long> {

    Optional<PaintingCategory> findByName(String name);

    Boolean existsByName(String name);
}
