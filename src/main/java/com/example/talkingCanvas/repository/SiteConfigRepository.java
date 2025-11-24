package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SiteConfig entity
 */
@Repository
public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {

    Optional<SiteConfig> findByConfigKey(String configKey);
}
