package com.example.talkingCanvas.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cart response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private String currency;
    private Integer totalItems;
    private LocalDateTime updatedAt;
}
