package com.example.talkingCanvas.dto.painting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for painting response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaintingResponse {

    private Long id;
    private String title;
    private String description;
    private String artistName;
    private BigDecimal price;
    private String currency;
    private Double height;
    private Double width;
    private Double depth;
    private String medium;
    private Integer yearCreated;
    private Boolean isAvailable;
    private Integer stockQuantity;
    private Boolean adminRecommendation;
    private String recommendationText;
    private List<PaintingImageDTO> images;
    private List<PaintingCertificateDTO> certificates;
    private List<CategoryDTO> categories;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private Long viewCount;
    private Long purchaseCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
