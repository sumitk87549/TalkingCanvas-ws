package com.example.talkingCanvas.dto.painting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for painting image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaintingImageDTO {

    private Long id;
    private String imageUrl;
    private String fileName;
    private String contentType;
    private Long size;
    private Integer displayOrder;
    private Boolean isPrimary;
}
