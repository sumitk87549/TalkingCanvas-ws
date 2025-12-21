package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.admin.DashboardStatsResponse;
import com.example.talkingCanvas.dto.admin.MonthlyRevenueDTO;
import com.example.talkingCanvas.dto.admin.PopularPaintingDTO;
import com.example.talkingCanvas.dto.common.PageResponse;
import com.example.talkingCanvas.dto.order.AdminContactDTO;
import com.example.talkingCanvas.dto.order.OrderResponse;
import com.example.talkingCanvas.dto.user.UserProfileResponse;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.Order;
import com.example.talkingCanvas.model.Painting;
import com.example.talkingCanvas.model.User;
import com.example.talkingCanvas.repository.*;
import com.example.talkingCanvas.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for admin operations
 */
@Service
@RequiredArgsConstructor
public class AdminService {

        private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final PaintingRepository paintingRepository;
        private final MapperUtil mapperUtil;

        @Value("${admin.default.name}")
        private String adminName;

        @Value("${admin.default.email}")
        private String adminEmail;

        @Value("${admin.default.uncle.name}")
        private String adminPhone;

        @Cacheable(value = "dashboard-stats")
        @Transactional(readOnly = true)
        public DashboardStatsResponse getDashboardStats() {
                logger.info("Fetching dashboard statistics");

                // User statistics
                Long totalUsers = userRepository.count();
                Long activeUsers = userRepository.countByIsActive(true);
                LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0)
                                .withSecond(0);
                Long newUsersThisMonth = userRepository.countUsersCreatedAfter(startOfMonth);

                // Order statistics
                Long totalOrders = orderRepository.count();
                Long pendingOrders = orderRepository.countByOrderStatus(Order.OrderStatus.PENDING);
                Long confirmedOrders = orderRepository.countByOrderStatus(Order.OrderStatus.CONFIRMED);
                Long shippedOrders = orderRepository.countByOrderStatus(Order.OrderStatus.SHIPPED);
                Long deliveredOrders = orderRepository.countByOrderStatus(Order.OrderStatus.DELIVERED);
                Long cancelledOrders = orderRepository.countByOrderStatus(Order.OrderStatus.CANCELLED);

                // Revenue statistics
                BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
                if (totalRevenue == null)
                        totalRevenue = BigDecimal.ZERO;

                BigDecimal revenueThisMonth = orderRepository.calculateRevenueAfter(startOfMonth);
                if (revenueThisMonth == null)
                        revenueThisMonth = BigDecimal.ZERO;

                LocalDateTime startOfYear = LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0)
                                .withSecond(0);
                BigDecimal revenueThisYear = orderRepository.calculateRevenueAfter(startOfYear);
                if (revenueThisYear == null)
                        revenueThisYear = BigDecimal.ZERO;

                // Painting statistics
                Long totalPaintings = paintingRepository.count();
                Long availablePaintings = paintingRepository.countByIsAvailable(true);
                Long outOfStockPaintings = totalPaintings - availablePaintings;

                // Popular paintings
                Pageable top5 = PageRequest.of(0, 5);
                List<PopularPaintingDTO> mostViewedPaintings = paintingRepository.findMostViewedPaintings(top5).stream()
                                .map(this::toPopularPaintingDTO)
                                .collect(Collectors.toList());

                List<PopularPaintingDTO> bestSellingPaintings = paintingRepository.findBestSellingPaintings(top5)
                                .stream()
                                .map(this::toPopularPaintingDTO)
                                .collect(Collectors.toList());

                // Order status distribution
                List<Object[]> statusDistributionData = orderRepository.getOrderStatusDistribution();
                Map<String, Long> orderStatusDistribution = new HashMap<>();
                for (Object[] data : statusDistributionData) {
                        orderStatusDistribution.put(data[0].toString(), ((Number) data[1]).longValue());
                }

                // Monthly revenue trend
                List<Object[]> revenueData = orderRepository.getMonthlyRevenueTrend();
                List<MonthlyRevenueDTO> monthlyRevenueTrend = revenueData.stream()
                                .limit(12)
                                .map(data -> MonthlyRevenueDTO.builder()
                                                .year(((Number) data[0]).intValue())
                                                .month(((Number) data[1]).intValue())
                                                .monthName(Month.of(((Number) data[1]).intValue()).name())
                                                .revenue((BigDecimal) data[2])
                                                .orderCount(((Number) data[3]).longValue())
                                                .build())
                                .collect(Collectors.toList());

                return DashboardStatsResponse.builder()
                                .totalUsers(totalUsers)
                                .activeUsers(activeUsers)
                                .newUsersThisMonth(newUsersThisMonth)
                                .totalOrders(totalOrders)
                                .pendingOrders(pendingOrders)
                                .confirmedOrders(confirmedOrders)
                                .shippedOrders(shippedOrders)
                                .deliveredOrders(deliveredOrders)
                                .cancelledOrders(cancelledOrders)
                                .totalRevenue(totalRevenue)
                                .revenueThisMonth(revenueThisMonth)
                                .revenueThisYear(revenueThisYear)
                                .currency("INR")
                                .totalPaintings(totalPaintings)
                                .availablePaintings(availablePaintings)
                                .outOfStockPaintings(outOfStockPaintings)
                                .mostViewedPaintings(mostViewedPaintings)
                                .bestSellingPaintings(bestSellingPaintings)
                                .orderStatusDistribution(orderStatusDistribution)
                                .monthlyRevenueTrend(monthlyRevenueTrend)
                                .build();
        }

        @Transactional(readOnly = true)
        public PageResponse<UserProfileResponse> getAllUsers(int page, int size) {
                logger.info("Fetching all users - page: {}, size: {}", page, size);
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                Page<User> userPage = userRepository.findAll(pageable);

                List<UserProfileResponse> content = userPage.getContent().stream()
                                .map(mapperUtil::toUserProfileResponse)
                                .collect(Collectors.toList());

                return PageResponse.<UserProfileResponse>builder()
                                .content(content)
                                .pageNumber(userPage.getNumber())
                                .pageSize(userPage.getSize())
                                .totalElements(userPage.getTotalElements())
                                .totalPages(userPage.getTotalPages())
                                .last(userPage.isLast())
                                .first(userPage.isFirst())
                                .build();
        }

        @Transactional(readOnly = true)
        public UserProfileResponse getUserById(Long userId) {
                logger.info("Fetching user: {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                return mapperUtil.toUserProfileResponse(user);
        }

        @Transactional
        public void toggleUserStatus(Long userId) {
                logger.info("Toggling status for user: {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                user.setIsActive(!user.getIsActive());
                userRepository.save(user);
                logger.info("User status toggled: {}", userId);
        }

        @Transactional
        public void deleteUser(Long userId) {
                logger.info("Deleting user: {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                user.setIsActive(false);
                userRepository.save(user);
                logger.info("User soft deleted: {}", userId);
        }

        @Transactional(readOnly = true)
        public PageResponse<OrderResponse> getAllOrders(int page, int size, String status) {
                logger.info("Fetching all orders - page: {}, size: {}, status: {}", page, size, status);
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<Order> orderPage;
                if (status != null && !status.isEmpty()) {
                        Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                        orderPage = orderRepository.findByOrderStatus(orderStatus, pageable);
                } else {
                        orderPage = orderRepository.findAll(pageable);
                }

                AdminContactDTO adminContact = AdminContactDTO.builder()
                                .name(adminName)
                                .email(adminEmail)
                                .phone(adminPhone)
                                .build();

                List<OrderResponse> content = orderPage.getContent().stream()
                                .map(order -> mapperUtil.toOrderResponse(order, adminContact))
                                .collect(Collectors.toList());

                return PageResponse.<OrderResponse>builder()
                                .content(content)
                                .pageNumber(orderPage.getNumber())
                                .pageSize(orderPage.getSize())
                                .totalElements(orderPage.getTotalElements())
                                .totalPages(orderPage.getTotalPages())
                                .last(orderPage.isLast())
                                .first(orderPage.isFirst())
                                .build();
        }

        @Transactional
        public OrderResponse updateOrderStatus(Long orderId, String status, String trackingInfo) {
                logger.info("Updating order status: {} to {}", orderId, status);
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

                order.setOrderStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
                if (trackingInfo != null) {
                        order.setTrackingInfo(trackingInfo);
                }

                Order updatedOrder = orderRepository.save(order);
                logger.info("Order status updated: {}", orderId);

                AdminContactDTO adminContact = AdminContactDTO.builder()
                                .name(adminName)
                                .email(adminEmail)
                                .phone(adminPhone)
                                .build();

                return mapperUtil.toOrderResponse(updatedOrder, adminContact);
        }

        private PopularPaintingDTO toPopularPaintingDTO(Painting painting) {
                String primaryImage = painting.getImages().stream()
                                .filter(img -> img.getIsPrimary())
                                .findFirst()
                                .map(img -> ServletUriComponentsBuilder.fromCurrentContextPath()
                                                .path("/api/images/")
                                                .path(String.valueOf(img.getId()))
                                                .toUriString())
                                .orElseGet(() -> painting.getImages().isEmpty() ? null
                                                : ServletUriComponentsBuilder.fromCurrentContextPath()
                                                                .path("/api/images/")
                                                                .path(String.valueOf(
                                                                                painting.getImages().get(0).getId()))
                                                                .toUriString());

                return PopularPaintingDTO.builder()
                                .paintingId(painting.getId())
                                .title(painting.getTitle())
                                .artistName(painting.getArtistName())
                                .price(painting.getPrice())
                                .primaryImage(primaryImage)
                                .viewCount(painting.getViewCount())
                                .purchaseCount(painting.getPurchaseCount())
                                .build();
        }

        @Transactional
        public void promoteToAdmin(Long userId) {
                logger.info("Promoting user to admin: {}", userId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                user.setRole(User.Role.ADMIN);
                userRepository.save(user);
                logger.info("User promoted to admin: {}", userId);
        }
}
