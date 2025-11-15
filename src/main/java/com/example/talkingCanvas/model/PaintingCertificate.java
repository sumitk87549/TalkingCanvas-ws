package com.example.talkingCanvas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing certificates for paintings
 */
@Entity
@Table(name = "painting_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaintingCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "painting_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Painting painting;

    @NotBlank(message = "Certificate URL is required")
    @Column(name = "certificate_url", nullable = false)
    private String certificateUrl;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column
    private String issuer;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(columnDefinition = "TEXT")
    private String description;
}
