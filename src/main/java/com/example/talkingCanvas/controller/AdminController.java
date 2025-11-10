package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.admin.DashboardStatsResponse;
import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.dto.common.PageResponse;
import com.example.talkingCanvas.dto.order.OrderResponse;
import com.example.talkingCanvas.dto.painting.CategoryDTO;
import com.example.talkingCanvas.dto.painting.CreatePaintingRequest;
import com.example.talkingCanvas.dto.painting.PaintingResponse;
import com.example.talkingCanvas.dto.user.UserProfileResponse;
import com.example.talkingCanvas.service.AdminService;
import com.example.talkingCanvas.service.PaintingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

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
    private final PaintingService paintingService;

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

    // Painting Management
    @PostMapping("/paintings")
    @Operation(summary = "Create painting", description = "Create a new painting listing")
    public ResponseEntity<ApiResponse<PaintingResponse>> createPainting(@Valid @RequestBody CreatePaintingRequest request) {
        PaintingResponse painting = paintingService.createPainting(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Painting created successfully", painting));
    }

    @PutMapping("/paintings/{paintingId}")
    @Operation(summary = "Update painting", description = "Update an existing painting")
    public ResponseEntity<ApiResponse<PaintingResponse>> updatePainting(
            @PathVariable Long paintingId,
            @Valid @RequestBody CreatePaintingRequest request) {
        PaintingResponse painting = paintingService.updatePainting(paintingId, request);
        return ResponseEntity.ok(ApiResponse.success("Painting updated successfully", painting));
    }

    @DeleteMapping("/paintings/{paintingId}")
    @Operation(summary = "Delete painting", description = "Soft delete a painting (mark as unavailable)")
    public ResponseEntity<ApiResponse<Void>> deletePainting(@PathVariable Long paintingId) {
        paintingService.deletePainting(paintingId);
        return ResponseEntity.ok(ApiResponse.success("Painting deleted successfully", null));
    }

    @PostMapping(value = "/paintings/{paintingId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload painting images", description = "Upload multiple images for a painting")
    public ResponseEntity<ApiResponse<Void>> uploadPaintingImages(
            @PathVariable Long paintingId,
            @RequestParam("files") MultipartFile[] files) {
        paintingService.uploadImages(paintingId, files);
        return ResponseEntity.ok(ApiResponse.success("Images uploaded successfully", null));
    }

    @PostMapping(value = "/paintings/{paintingId}/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload certificate", description = "Upload a certificate for a painting")
    public ResponseEntity<ApiResponse<Void>> uploadCertificate(
            @PathVariable Long paintingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "issuer", required = false) String issuer,
            @RequestParam(value = "issueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestParam(value = "description", required = false) String description) {
        paintingService.uploadCertificate(paintingId, file, title, issuer, issueDate, description);
        return ResponseEntity.ok(ApiResponse.success("Certificate uploaded successfully", null));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create category", description = "Create a new painting category")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        CategoryDTO category = paintingService.createCategory(name, description);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
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
}
