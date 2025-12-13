package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.admin.DashboardStatsResponse;
import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.dto.common.PageResponse;
import com.example.talkingCanvas.dto.order.OrderResponse;

import com.example.talkingCanvas.dto.user.UserProfileResponse;
import com.example.talkingCanvas.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for admin operations
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Admin Management", description = "Admin dashboard and management APIs")
public class AdminController {

    private final AdminService adminService;

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    @Operation(summary = "Get dashboard statistics", description = "Get comprehensive dashboard statistics for admin")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // User Management
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Get paginated list of all users")
    public ResponseEntity<ApiResponse<PageResponse<UserProfileResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserProfileResponse> users = adminService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user by ID", description = "Get detailed information about a specific user")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable Long userId) {
        UserProfileResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Toggle user status", description = "Enable or disable a user account")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(@PathVariable Long userId) {
        adminService.toggleUserStatus(userId);
        return ResponseEntity.ok(ApiResponse.success("User status updated", null));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }

    // Order Management
    @GetMapping("/orders")
    @Operation(summary = "Get all orders", description = "Get paginated list of all orders with optional status filter")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        PageResponse<OrderResponse> orders = adminService.getAllOrders(page, size, status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/orders/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update order status and tracking information")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            @RequestParam(required = false) String trackingInfo) {
        OrderResponse order = adminService.updateOrderStatus(orderId, status, trackingInfo);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }

    @PutMapping("/users/{userId}/promote")
    @Operation(summary = "Promote user to admin", description = "Grant admin privileges to a user")
    public ResponseEntity<ApiResponse<Void>> promoteToAdmin(@PathVariable Long userId) {
        adminService.promoteToAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success("User promoted to admin successfully", null));
    }
}
