package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.dto.wishlist.AddToWishlistRequest;
import com.example.talkingCanvas.dto.wishlist.WishlistResponse;
import com.example.talkingCanvas.security.UserPrincipal;
import com.example.talkingCanvas.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * Controller for wishlist operations
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "User wishlist management APIs")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get wishlist", description = "Get current user's wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser != null) {
            WishlistResponse wishlist = wishlistService.getWishlist(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(wishlist));
        } else {
            // Return empty wishlist for anonymous users
            WishlistResponse emptyWishlist = WishlistResponse.builder()
                    .items(new ArrayList<>())
                    .totalItems(0)
                    .build();
            return ResponseEntity.ok(ApiResponse.success(emptyWishlist));
        }
    }

    @PostMapping("/add")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add to wishlist", description = "Add a painting to the wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> addToWishlist(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AddToWishlistRequest request) {
        WishlistResponse wishlist = wishlistService.addToWishlist(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Painting added to wishlist", wishlist));
    }

    @DeleteMapping("/items/{itemId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove from wishlist", description = "Remove a painting from the wishlist")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long itemId) {
        WishlistResponse wishlist = wishlistService.removeFromWishlist(currentUser.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Painting removed from wishlist", wishlist));
    }

    @GetMapping("/check/{paintingId}")
    @Operation(summary = "Check if in wishlist", description = "Check if a painting is in the user's wishlist")
    public ResponseEntity<ApiResponse<Boolean>> isInWishlist(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long paintingId) {
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.success(false));
        }
        boolean inWishlist = wishlistService.isInWishlist(currentUser.getId(), paintingId);
        return ResponseEntity.ok(ApiResponse.success(inWishlist));
    }

    @GetMapping("/count")
    @Operation(summary = "Get wishlist count", description = "Get total number of items in the wishlist")
    public ResponseEntity<ApiResponse<Long>> getWishlistItemCount(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.ok(ApiResponse.success(0L));
        }
        Long count = wishlistService.getWishlistItemCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @DeleteMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Clear wishlist", description = "Remove all items from the wishlist")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(@AuthenticationPrincipal UserPrincipal currentUser) {
        wishlistService.clearWishlist(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Wishlist cleared", null));
    }
}
