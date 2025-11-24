package com.example.talkingCanvas.repository;

import com.example.talkingCanvas.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByUserId(Long userId);

    Page<Order> findByOrderStatus(Order.OrderStatus orderStatus, Pageable pageable);

    Long countByOrderStatus(Order.OrderStatus orderStatus);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus != 'CANCELLED'")
    BigDecimal calculateTotalRevenue();

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus != 'CANCELLED' AND o.createdAt >= :startDate")
    BigDecimal calculateRevenueAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT o.orderStatus, COUNT(o) FROM Order o GROUP BY o.orderStatus")
    List<Object[]> getOrderStatusDistribution();

    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), SUM(o.totalAmount), COUNT(o) " +
           "FROM Order o WHERE o.orderStatus != 'CANCELLED' " +
           "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
           "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> getMonthlyRevenueTrend();
}
