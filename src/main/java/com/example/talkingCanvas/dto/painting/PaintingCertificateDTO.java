package com.example.talkingCanvas.dto.painting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for painting certificate
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaintingCertificateDTO {

    private Long id;
    private String certificateUrl;
    private String title;
    private String issuer;
    private LocalDate issueDate;
    private String description;
}
