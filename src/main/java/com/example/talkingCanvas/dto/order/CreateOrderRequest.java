package com.example.talkingCanvas.dto.order;

import com.example.talkingCanvas.dto.user.AddressDTO;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    // @NotNull(message = "Delivery address is required")
    @Valid
    private AddressDTO deliveryAddress;

    private String notes;
}
