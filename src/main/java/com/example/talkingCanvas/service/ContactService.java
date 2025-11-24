package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.contact.ContactRequest;
import com.example.talkingCanvas.model.ContactMessage;
import com.example.talkingCanvas.repository.ContactMessageRepository;
import com.example.talkingCanvas.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for contact form operations
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;

    @Transactional
    public void submitContactForm(ContactRequest request) {
        logger.info("Processing contact form submission from: {}", request.getEmail());

        ContactMessage message = ContactMessage.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .isRead(false)
                .build();

        contactMessageRepository.save(message);

        // Send notification to admin
        emailService.sendContactFormNotification(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getSubject(),
                request.getMessage()
        );

        logger.info("Contact form submission processed successfully");
    }
}
