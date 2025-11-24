package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.dto.contact.ContactRequest;
import com.example.talkingCanvas.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for contact form operations
 */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@Tag(name = "Contact", description = "Contact form submission API")
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/submit")
    @Operation(summary = "Submit contact form", description = "Submit a contact form message")
    public ResponseEntity<ApiResponse<Void>> submitContactForm(@Valid @RequestBody ContactRequest request) {
        contactService.submitContactForm(request);
        return ResponseEntity.ok(ApiResponse.success("Message sent successfully. We'll get back to you soon!", null));
    }
}
