package com.example.talkingCanvas.dto.order;

import com.example.talkingCanvas.dto.user.AddressDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for order response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderNumber;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private String currency;
    private AddressDTO deliveryAddress;
    private String orderStatus;
    private String paymentMethod;
    private String trackingInfo;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AdminContactDTO adminContact;
    private com.example.talkingCanvas.dto.user.UserProfileResponse user;
}
