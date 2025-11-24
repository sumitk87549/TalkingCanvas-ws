package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.dto.common.PageResponse;
import com.example.talkingCanvas.dto.painting.CategoryDTO;
import com.example.talkingCanvas.dto.painting.PaintingResponse;
import com.example.talkingCanvas.service.PaintingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for public painting endpoints
 */
@RestController
@RequestMapping("/api/paintings")
@RequiredArgsConstructor
@Tag(name = "Paintings", description = "Public painting browsing and search APIs")
public class PaintingController {

    private final PaintingService paintingService;

    @GetMapping
    @Operation(summary = "Get all paintings", description = "Get paginated list of all available paintings")
    public ResponseEntity<ApiResponse<PageResponse<PaintingResponse>>> getAllPaintings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        System.out.println("Inside getAllPaintings in PaintingController.java");
        PageResponse<PaintingResponse> paintings = paintingService.getAllPaintings(page, size, sortBy, sortDirection);
        System.out.println(paintings);
        return ResponseEntity.ok(ApiResponse.success(paintings));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get painting by ID", description = "Get detailed information about a specific painting")
    public ResponseEntity<ApiResponse<PaintingResponse>> getPaintingById(@PathVariable Long id) {
        PaintingResponse painting = paintingService.getPaintingById(id);
        return ResponseEntity.ok(ApiResponse.success(painting));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured paintings", description = "Get paintings recommended by admin")
    public ResponseEntity<ApiResponse<PageResponse<PaintingResponse>>> getFeaturedPaintings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PageResponse<PaintingResponse> paintings = paintingService.getFeaturedPaintings(page, size);
        return ResponseEntity.ok(ApiResponse.success(paintings));
    }

    @GetMapping("/search")
    @Operation(summary = "Search paintings", description = "Search paintings by title, description, artist, or medium")
    public ResponseEntity<ApiResponse<PageResponse<PaintingResponse>>> searchPaintings(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PageResponse<PaintingResponse> paintings = paintingService.searchPaintings(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(paintings));
    }

    @GetMapping("/filter/price")
    @Operation(summary = "Filter by price range", description = "Filter paintings by minimum and maximum price")
    public ResponseEntity<ApiResponse<PageResponse<PaintingResponse>>> filterByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        PageResponse<PaintingResponse> paintings = paintingService.filterByPriceRange(minPrice, maxPrice, page, size);
        return ResponseEntity.ok(ApiResponse.success(paintings));
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "Get list of all painting categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories() {
        List<CategoryDTO> categories = paintingService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
