package com.example.talkingCanvas.util;

import com.example.talkingCanvas.dto.cart.CartItemDTO;
import com.example.talkingCanvas.dto.cart.CartResponse;
import com.example.talkingCanvas.dto.order.AdminContactDTO;
import com.example.talkingCanvas.dto.order.OrderItemDTO;
import com.example.talkingCanvas.dto.order.OrderResponse;
import com.example.talkingCanvas.dto.painting.*;
import com.example.talkingCanvas.dto.user.AddressDTO;
import com.example.talkingCanvas.dto.user.UserProfileResponse;
import com.example.talkingCanvas.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between entities and DTOs
 */
@Component
public class MapperUtil {

    // User Mapping
    public UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .contactNumber(user.getContactNumber())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .addresses(user.getAddresses().stream()
                        .map(this::toAddressDTO)
                        .collect(Collectors.toList()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public AddressDTO toAddressDTO(Address address) {
        return AddressDTO.builder()
                .id(address.getId())
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pincode(address.getPincode())
                .isDefault(address.getIsDefault())
                .build();
    }

    // Painting Mapping
    public PaintingResponse toPaintingResponse(Painting painting) {
        return PaintingResponse.builder()
                .id(painting.getId())
                .title(painting.getTitle())
                .description(painting.getDescription())
                .artistName(painting.getArtistName())
                .price(painting.getPrice())
                .currency(painting.getCurrency())
                .height(painting.getHeight())
                .width(painting.getWidth())
                .depth(painting.getDepth())
                .medium(painting.getMedium())
                .yearCreated(painting.getYearCreated())
                .isAvailable(painting.getIsAvailable())
                .stockQuantity(painting.getStockQuantity())
                .adminRecommendation(painting.getAdminRecommendation())
                .recommendationText(painting.getRecommendationText())
                .images(painting.getImages().stream()
                        .map(this::toPaintingImageDTO)
                        .collect(Collectors.toList()))
                .certificates(painting.getCertificates().stream()
                        .map(this::toPaintingCertificateDTO)
                        .collect(Collectors.toList()))
                .categories(painting.getCategories().stream()
                        .map(this::toCategoryDTO)
                        .collect(Collectors.toList()))
                .seoTitle(painting.getSeoTitle())
                .seoDescription(painting.getSeoDescription())
                .seoKeywords(painting.getSeoKeywords())
                .viewCount(painting.getViewCount())
                .purchaseCount(painting.getPurchaseCount())
                .createdAt(painting.getCreatedAt())
                .updatedAt(painting.getUpdatedAt())
                .build();
    }

    public PaintingImageDTO toPaintingImageDTO(PaintingImage image) {
        return PaintingImageDTO.builder()
                .id(image.getId())
                .imageUrl("/uploads/" + image.getImageUrl())
                .displayOrder(image.getDisplayOrder())
                .isPrimary(image.getIsPrimary())
                .build();
    }

    public PaintingCertificateDTO toPaintingCertificateDTO(PaintingCertificate certificate) {
        return PaintingCertificateDTO.builder()
                .id(certificate.getId())
                .certificateUrl("/uploads/" + certificate.getCertificateUrl())
                .title(certificate.getTitle())
                .issuer(certificate.getIssuer())
                .issueDate(certificate.getIssueDate())
                .description(certificate.getDescription())
                .build();
    }

    public CategoryDTO toCategoryDTO(PaintingCategory category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    // Cart Mapping
    public CartResponse toCartResponse(Cart cart) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(this::toCartItemDTO)
                .collect(Collectors.toList());

        BigDecimal totalAmount = items.stream()
                .map(CartItemDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(items)
                .totalAmount(totalAmount)
                .currency("INR")
                .totalItems(items.size())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public CartItemDTO toCartItemDTO(CartItem item) {
        Painting painting = item.getPainting();
        String primaryImage = painting.getImages().stream()
                .filter(PaintingImage::getIsPrimary)
                .findFirst()
                .map(img -> "/uploads/" + img.getImageUrl())
                .orElse(painting.getImages().isEmpty() ? null : "/uploads/" + painting.getImages().get(0).getImageUrl());

        BigDecimal subtotal = painting.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemDTO.builder()
                .id(item.getId())
                .paintingId(painting.getId())
                .paintingTitle(painting.getTitle())
                .artistName(painting.getArtistName())
                .primaryImage(primaryImage)
                .price(painting.getPrice())
                .currency(painting.getCurrency())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .isAvailable(painting.getIsAvailable())
                .stockQuantity(painting.getStockQuantity())
                .build();
    }

    // Order Mapping
    public OrderResponse toOrderResponse(Order order, AdminContactDTO adminContact) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .items(order.getItems().stream()
                        .map(this::toOrderItemDTO)
                        .collect(Collectors.toList()))
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .deliveryAddress(toAddressDTO(order.getDeliveryAddress()))
                .orderStatus(order.getOrderStatus().name())
                .paymentMethod(order.getPaymentMethod())
                .trackingInfo(order.getTrackingInfo())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .adminContact(adminContact)
                .build();
    }

    public OrderItemDTO toOrderItemDTO(OrderItem item) {
        BigDecimal subtotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));

        return OrderItemDTO.builder()
                .id(item.getId())
                .paintingId(item.getPainting().getId())
                .paintingTitle(item.getPaintingTitle())
                .artistName(item.getArtistName())
                .quantity(item.getQuantity())
                .priceAtPurchase(item.getPriceAtPurchase())
                .subtotal(subtotal)
                .build();
    }
}
