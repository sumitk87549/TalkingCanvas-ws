package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.dto.common.PageResponse;
import com.example.talkingCanvas.dto.painting.CategoryDTO;
import com.example.talkingCanvas.dto.painting.CreatePaintingRequest;
import com.example.talkingCanvas.dto.painting.PaintingResponse;
import com.example.talkingCanvas.service.PaintingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Controller for administrative painting operations
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Painting Management", description = "APIs for managing paintings (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaintingController {

    private final PaintingService paintingService;

    @GetMapping("/paintings")
    @Operation(summary = "Get all paintings for admin", description = "Get paginated list of all paintings including unavailable ones")
    public ResponseEntity<ApiResponse<PageResponse<PaintingResponse>>> getAllPaintings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        PageResponse<PaintingResponse> paintings = paintingService.getAllPaintingsForAdmin(page, size, sortBy,
                sortDirection);
        return ResponseEntity.ok(ApiResponse.success(paintings));
    }

    @PostMapping("/paintings")
    @Operation(summary = "Create a new painting", description = "Create a new painting entry")
    public ResponseEntity<ApiResponse<PaintingResponse>> createPainting(
            @Valid @RequestBody CreatePaintingRequest request) {
        PaintingResponse painting = paintingService.createPainting(request);
        return ResponseEntity.ok(ApiResponse.success(painting));
    }

    @PutMapping("/paintings/{id}")
    @Operation(summary = "Update a painting", description = "Update an existing painting")
    public ResponseEntity<ApiResponse<PaintingResponse>> updatePainting(
            @PathVariable Long id,
            @Valid @RequestBody CreatePaintingRequest request) {
        PaintingResponse painting = paintingService.updatePainting(id, request);
        return ResponseEntity.ok(ApiResponse.success(painting));
    }

    @DeleteMapping("/paintings/{id}")
    @Operation(summary = "Delete a painting", description = "Soft delete a painting")
    public ResponseEntity<ApiResponse<Void>> deletePainting(@PathVariable Long id) {
        paintingService.deletePainting(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping(value = "/paintings/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload painting images", description = "Upload multiple images for a painting")
    public ResponseEntity<ApiResponse<Void>> uploadImages(
            @PathVariable Long id,
            @RequestParam("files") MultipartFile[] files) {
        paintingService.uploadImages(id, files);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/paintings/{id}/images/{imageId}")
    @Operation(summary = "Delete painting image", description = "Delete a specific image from a painting")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable Long id,
            @PathVariable Long imageId) {
        paintingService.deleteImage(id, imageId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping(value = "/paintings/{id}/certificates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload painting certificate", description = "Upload a certificate for a painting")
    public ResponseEntity<ApiResponse<Void>> uploadCertificate(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "issuer", required = false) String issuer,
            @RequestParam(value = "issueDate", required = false) LocalDate issueDate,
            @RequestParam(value = "description", required = false) String description) {
        paintingService.uploadCertificate(id, file, title, issuer, issueDate, description);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/categories")
    @Operation(summary = "Create a new category", description = "Create a new painting category")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description) {
        CategoryDTO category = paintingService.createCategory(name, description);
        return ResponseEntity.ok(ApiResponse.success(category));
    }
}
