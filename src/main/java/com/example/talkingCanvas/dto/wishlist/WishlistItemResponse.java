package com.example.talkingCanvas.dto.wishlist;

import com.example.talkingCanvas.dto.painting.PaintingResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for wishlist item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemResponse {

    private Long id;
    private Long paintingId;
    private PaintingResponse painting;
    private LocalDateTime addedAt;
}
