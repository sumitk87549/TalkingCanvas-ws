package com.example.talkingCanvas.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for cart item
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDTO {

    private Long id;
    private Long paintingId;
    private String paintingTitle;
    private String artistName;
    private String primaryImage;
    private BigDecimal price;
    private String currency;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean isAvailable;
    private Integer stockQuantity;
}
