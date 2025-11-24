package com.example.talkingCanvas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Entity representing images for paintings
 */
@Entity
@Table(name = "painting_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaintingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "painting_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Painting painting;

    // Legacy column retained for backward compatibility with existing NOT NULL schema
    @Column(name = "image_url", nullable = false)
    @Builder.Default
    private String imageUrl = "INLINE";

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_data", nullable = true)
    private byte[] data;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size")
    private Long size;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
}

