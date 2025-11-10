package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for ContactMessage entity
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findByIsRead(Boolean isRead, Pageable pageable);

    Long countByIsRead(Boolean isRead);
}
