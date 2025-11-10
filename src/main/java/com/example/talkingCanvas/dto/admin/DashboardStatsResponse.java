package com.example.talkingCanvas.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for admin dashboard statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    // User Statistics
    private Long totalUsers;
    private Long activeUsers;
    private Long newUsersThisMonth;

    // Order Statistics
    private Long totalOrders;
    private Long pendingOrders;
    private Long confirmedOrders;
    private Long shippedOrders;
    private Long deliveredOrders;
    private Long cancelledOrders;

    // Revenue Statistics
    private BigDecimal totalRevenue;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenueThisYear;
    private String currency;

    // Painting Statistics
    private Long totalPaintings;
    private Long availablePaintings;
    private Long outOfStockPaintings;

    // Popular Items
    private List<PopularPaintingDTO> mostViewedPaintings;
    private List<PopularPaintingDTO> bestSellingPaintings;

    // Order Status Distribution
    private Map<String, Long> orderStatusDistribution;

    // Monthly Revenue Trend
    private List<MonthlyRevenueDTO> monthlyRevenueTrend;
}
