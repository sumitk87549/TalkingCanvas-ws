package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.order.AdminContactDTO;
import com.example.talkingCanvas.dto.order.CreateOrderRequest;
import com.example.talkingCanvas.dto.order.OrderResponse;
import com.example.talkingCanvas.dto.user.AddressDTO;
import com.example.talkingCanvas.exception.BadRequestException;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.*;
import com.example.talkingCanvas.repository.*;
import com.example.talkingCanvas.util.EmailService;
import com.example.talkingCanvas.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for order operations
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PaintingRepository paintingRepository;
    private final EmailService emailService;
    private final MapperUtil mapperUtil;

    @Value("${admin.default.name}")
    private String adminName;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.uncle.name}")
    private String adminPhone;

    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        logger.info("Creating order for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        // Validate stock availability
        for (CartItem item : cart.getItems()) {
            Painting painting = item.getPainting();
            if (!painting.getIsAvailable()) {
                throw new BadRequestException("Painting '" + painting.getTitle() + "' is no longer available");
            }
            if (painting.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for '" + painting.getTitle() + "'. Available: " + painting.getStockQuantity());
            }
        }

        // Create delivery address
        AddressDTO addressDTO = request.getDeliveryAddress();
        Address deliveryAddress = Address.builder()
                .user(user)
                .street(addressDTO.getStreet())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .country(addressDTO.getCountry())
                .pincode(addressDTO.getPincode())
                .isDefault(false)
                .build();
        deliveryAddress = addressRepository.save(deliveryAddress);

        // Calculate total amount
        BigDecimal totalAmount = cart.getItems().stream()
                .map(item -> item.getPainting().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate order number
        String orderNumber = generateOrderNumber();

        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .totalAmount(totalAmount)
                .currency("INR")
                .deliveryAddress(deliveryAddress)
                .orderStatus(Order.OrderStatus.PENDING)
                .paymentMethod("COD")
                .notes(request.getNotes())
                .build();

        // Create order items
        for (CartItem cartItem : cart.getItems()) {
            Painting painting = cartItem.getPainting();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .painting(painting)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(painting.getPrice())
                    .paintingTitle(painting.getTitle())
                    .artistName(painting.getArtistName())
                    .build();
            order.addItem(orderItem);

            // Update stock quantity
            painting.setStockQuantity(painting.getStockQuantity() - cartItem.getQuantity());
            if (painting.getStockQuantity() == 0) {
                painting.setIsAvailable(false);
            }
            // Increment purchase count
            painting.incrementPurchaseCount();
            paintingRepository.save(painting);
        }

        Order savedOrder = orderRepository.save(order);

        // Clear cart
        cart.clear();
        cartRepository.save(cart);

        // Send order confirmation email
        emailService.sendOrderConfirmationEmail(
                user.getEmail(),
                user.getName(),
                orderNumber,
                totalAmount.toString() + " INR"
        );

        logger.info("Order created successfully: {}", orderNumber);

        AdminContactDTO adminContact = AdminContactDTO.builder()
                .name(adminName)
                .email(adminEmail)
                .phone(adminPhone)
                .build();

        return mapperUtil.toOrderResponse(savedOrder, adminContact);
    }

    public OrderResponse getOrderById(Long userId, Long orderId) {
        logger.info("Fetching order: {} for user: {}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to user");
        }

        AdminContactDTO adminContact = AdminContactDTO.builder()
                .name(adminName)
                .email(adminEmail)
                .phone(adminPhone)
                .build();

        return mapperUtil.toOrderResponse(order, adminContact);
    }

    public List<OrderResponse> getUserOrders(Long userId, int page, int size) {
        logger.info("Fetching orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        AdminContactDTO adminContact = AdminContactDTO.builder()
                .name(adminName)
                .email(adminEmail)
                .phone(adminPhone)
                .build();

        return orderRepository.findByUserId(userId, pageable)
                .stream()
                .map(order -> mapperUtil.toOrderResponse(order, adminContact))
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        logger.info("Cancelling order: {} for user: {}", orderId, userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to user");
        }

        if (order.getOrderStatus() == Order.OrderStatus.DELIVERED ||
            order.getOrderStatus() == Order.OrderStatus.CANCELLED) {
            throw new BadRequestException("Order cannot be cancelled");
        }

        order.setOrderStatus(Order.OrderStatus.CANCELLED);

        // Restore stock
        for (OrderItem item : order.getItems()) {
            Painting painting = item.getPainting();
            painting.setStockQuantity(painting.getStockQuantity() + item.getQuantity());
            painting.setIsAvailable(true);
            paintingRepository.save(painting);
        }

        Order updatedOrder = orderRepository.save(order);
        logger.info("Order cancelled: {}", orderId);

        AdminContactDTO adminContact = AdminContactDTO.builder()
                .name(adminName)
                .email(adminEmail)
                .phone(adminPhone)
                .build();

        return mapperUtil.toOrderResponse(updatedOrder, adminContact);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "TC" + timestamp;
    }
}
