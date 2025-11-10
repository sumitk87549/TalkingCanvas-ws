package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.cart.AddToCartRequest;
import com.example.talkingCanvas.dto.cart.CartResponse;
import com.example.talkingCanvas.exception.BadRequestException;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.Cart;
import com.example.talkingCanvas.model.CartItem;
import com.example.talkingCanvas.model.Painting;
import com.example.talkingCanvas.model.User;
import com.example.talkingCanvas.repository.CartItemRepository;
import com.example.talkingCanvas.repository.CartRepository;
import com.example.talkingCanvas.repository.PaintingRepository;
import com.example.talkingCanvas.repository.UserRepository;
import com.example.talkingCanvas.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for cart operations
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final PaintingRepository paintingRepository;
    private final UserRepository userRepository;
    private final MapperUtil mapperUtil;

    @Transactional
    public CartResponse getCart(Long userId) {
        logger.info("Fetching cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        return mapperUtil.toCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        logger.info("Adding item to cart for user: {}", userId);
        
        Cart cart = getOrCreateCart(userId);
        Painting painting = paintingRepository.findById(request.getPaintingId())
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", request.getPaintingId()));

        // Validate painting availability
        if (!painting.getIsAvailable()) {
            throw new BadRequestException("Painting is not available");
        }

        if (painting.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + painting.getStockQuantity());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndPaintingId(cart.getId(), painting.getId());

        if (existingItem.isPresent()) {
            // Update quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            
            if (painting.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Insufficient stock. Available: " + painting.getStockQuantity());
            }
            
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            // Add new item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .painting(painting)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        Cart updatedCart = cartRepository.save(cart);
        logger.info("Item added to cart for user: {}", userId);
        return mapperUtil.toCartResponse(updatedCart);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, Long itemId, Integer quantity) {
        logger.info("Updating cart item: {} for user: {}", itemId, userId);
        
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to user");
        }

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        if (item.getPainting().getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock. Available: " + item.getPainting().getStockQuantity());
        }

        item.setQuantity(quantity);
        cartItemRepository.save(item);

        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        logger.info("Cart item updated for user: {}", userId);
        return mapperUtil.toCartResponse(updatedCart);
    }

    @Transactional
    public CartResponse removeCartItem(Long userId, Long itemId) {
        logger.info("Removing cart item: {} for user: {}", itemId, userId);
        
        Cart cart = getOrCreateCart(userId);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to user");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);

        Cart updatedCart = cartRepository.findById(cart.getId()).get();
        logger.info("Cart item removed for user: {}", userId);
        return mapperUtil.toCartResponse(updatedCart);
    }

    @Transactional
    public void clearCart(Long userId) {
        logger.info("Clearing cart for user: {}", userId);
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
        logger.info("Cart cleared for user: {}", userId);
    }

    private Cart getOrCreateCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }
}
