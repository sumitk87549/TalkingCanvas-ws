package com.example.talkingCanvas.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for monthly revenue data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueDTO {

    private Integer year;
    private Integer month;
    private String monthName;
    private BigDecimal revenue;
    private Long orderCount;
}
