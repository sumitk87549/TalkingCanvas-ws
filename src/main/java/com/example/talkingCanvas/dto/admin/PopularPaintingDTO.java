package com.example.talkingCanvas.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for popular painting statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularPaintingDTO {

    private Long paintingId;
    private String title;
    private String artistName;
    private BigDecimal price;
    private String primaryImage;
    private Long viewCount;
    private Long purchaseCount;
}
