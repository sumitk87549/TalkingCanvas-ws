package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.PaintingCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for PaintingCertificate entity
 */
@Repository
public interface PaintingCertificateRepository extends JpaRepository<PaintingCertificate, Long> {

    List<PaintingCertificate> findByPaintingId(Long paintingId);
}
