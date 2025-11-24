package com.example.talkingCanvas.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin contact information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminContactDTO {

    private String name;
    private String email;
    private String phone;
}
