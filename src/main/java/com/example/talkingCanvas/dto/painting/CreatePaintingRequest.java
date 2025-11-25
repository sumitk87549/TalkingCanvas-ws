package com.example.talkingCanvas.dto.painting;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a painting
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaintingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String artistName; // Optional, will use default if not provided

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Builder.Default
    private String currency = "INR";

    @NotNull(message = "Height is required")
    private Double height;

    @NotNull(message = "Width is required")
    private Double width;

    private Double depth;

    @NotBlank(message = "Medium is required")
    private String medium;

    private Integer yearCreated;

    @Builder.Default
    private Boolean isAvailable = true;

    @Builder.Default
    private Integer stockQuantity = 1;

    @Builder.Default
    private Boolean adminRecommendation = false;

    private String recommendationText;

    private List<Long> categoryIds;

    private String seoTitle;

    private String seoDescription;

    private String seoKeywords;
}
