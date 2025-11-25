package com.example.talkingCanvas.controller;

import com.example.talkingCanvas.dto.cart.AddToCartRequest;
import com.example.talkingCanvas.dto.cart.CartResponse;
import com.example.talkingCanvas.dto.common.ApiResponse;
import com.example.talkingCanvas.security.UserPrincipal;
import com.example.talkingCanvas.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Controller for cart operations
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Management", description = "Shopping cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Get current user's shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser != null) {
            CartResponse cart = cartService.getCart(currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success(cart));
        } else {
            // Return empty cart for anonymous users
            CartResponse emptyCart = CartResponse.builder()
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .totalItems(0)
                .build();
            return ResponseEntity.ok(ApiResponse.success(emptyCart));
        }
    }

    @PostMapping("/add")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Add to cart", description = "Add a painting to the shopping cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse cart = cartService.addToCart(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update cart item", description = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        CartResponse cart = cartService.updateCartItem(currentUser.getId(), itemId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Remove cart item", description = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long itemId) {
        CartResponse cart = cartService.removeCartItem(currentUser.getId(), itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Clear cart", description = "Remove all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserPrincipal currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
