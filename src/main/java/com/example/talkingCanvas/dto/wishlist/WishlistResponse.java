package com.example.talkingCanvas.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for wishlist
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse {

    private Long id;

    @Builder.Default
    private List<WishlistItemResponse> items = new ArrayList<>();

    private Integer totalItems;
}
