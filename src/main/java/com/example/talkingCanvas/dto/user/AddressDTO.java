package com.example.talkingCanvas.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for address data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {

    private Long id;

    // @NotBlank(message = "Street address is required")
    private String street;

    // @NotBlank(message = "City is required")
    private String city;

    private String state;

    // @NotBlank(message = "Country is required")
    private String country;

    private String pincode;

    private Boolean isDefault;
}
