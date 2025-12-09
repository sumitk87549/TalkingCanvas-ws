package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.wishlist.AddToWishlistRequest;
import com.example.talkingCanvas.dto.wishlist.WishlistResponse;
import com.example.talkingCanvas.exception.BadRequestException;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.Painting;
import com.example.talkingCanvas.model.User;
import com.example.talkingCanvas.model.Wishlist;
import com.example.talkingCanvas.model.WishlistItem;
import com.example.talkingCanvas.repository.PaintingRepository;
import com.example.talkingCanvas.repository.UserRepository;
import com.example.talkingCanvas.repository.WishlistItemRepository;
import com.example.talkingCanvas.repository.WishlistRepository;
import com.example.talkingCanvas.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for wishlist operations
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final PaintingRepository paintingRepository;
    private final UserRepository userRepository;
    private final MapperUtil mapperUtil;

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long userId) {
        logger.info("Fetching wishlist for user: {}", userId);
        Wishlist wishlist = getOrCreateWishlist(userId);
        return mapperUtil.toWishlistResponse(wishlist);
    }

    @Transactional
    public WishlistResponse addToWishlist(Long userId, AddToWishlistRequest request) {
        logger.info("Adding painting {} to wishlist for user: {}", request.getPaintingId(), userId);

        Wishlist wishlist = getOrCreateWishlist(userId);
        Painting painting = paintingRepository.findById(request.getPaintingId())
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", request.getPaintingId()));

        // Check if item already exists in wishlist
        Optional<WishlistItem> existingItem = wishlistItemRepository
                .findByWishlistIdAndPaintingId(wishlist.getId(), painting.getId());

        if (existingItem.isPresent()) {
            logger.warn("Painting {} already in wishlist for user: {}", request.getPaintingId(), userId);
            throw new BadRequestException("Painting is already in wishlist");
        }

        // Add new item
        WishlistItem newItem = WishlistItem.builder()
                .wishlist(wishlist)
                .painting(painting)
                .build();
        wishlist.addItem(newItem);

        Wishlist updatedWishlist = wishlistRepository.save(wishlist);
        logger.info("Painting {} added to wishlist for user: {}", request.getPaintingId(), userId);
        return mapperUtil.toWishlistResponse(updatedWishlist);
    }

    @Transactional
    public WishlistResponse removeFromWishlist(Long userId, Long itemId) {
        logger.info("Removing wishlist item: {} for user: {}", itemId, userId);

        Wishlist wishlist = getOrCreateWishlist(userId);
        WishlistItem item = wishlistItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("WishlistItem", "id", itemId));

        if (!item.getWishlist().getId().equals(wishlist.getId())) {
            throw new BadRequestException("Wishlist item does not belong to user");
        }

        wishlist.removeItem(item);
        wishlistItemRepository.delete(item);

        Wishlist updatedWishlist = wishlistRepository.findById(wishlist.getId()).get();
        logger.info("Wishlist item removed for user: {}", userId);
        return mapperUtil.toWishlistResponse(updatedWishlist);
    }

    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long paintingId) {
        logger.info("Checking if painting {} is in wishlist for user: {}", paintingId, userId);
        return wishlistRepository.existsByUserIdAndPaintingId(userId, paintingId);
    }

    @Transactional(readOnly = true)
    public Long getWishlistItemCount(Long userId) {
        logger.info("Getting wishlist item count for user: {}", userId);
        return wishlistRepository.getTotalItemsCountByUserId(userId);
    }

    @Transactional
    public void clearWishlist(Long userId) {
        logger.info("Clearing wishlist for user: {}", userId);
        Wishlist wishlist = getOrCreateWishlist(userId);
        wishlist.clear();
        wishlistRepository.save(wishlist);
        logger.info("Wishlist cleared for user: {}", userId);
    }

    private Wishlist getOrCreateWishlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            Wishlist newWishlist = Wishlist.builder().user(user).build();
            return wishlistRepository.save(newWishlist);
        });
    }
}
