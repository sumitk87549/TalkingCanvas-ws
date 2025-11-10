package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.order.OrderResponse;
import com.example.talkingCanvas.dto.user.ChangePasswordRequest;
import com.example.talkingCanvas.dto.user.UpdateProfileRequest;
import com.example.talkingCanvas.dto.user.UserProfileResponse;
import com.example.talkingCanvas.exception.BadRequestException;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.User;
import com.example.talkingCanvas.repository.OrderRepository;
import com.example.talkingCanvas.repository.UserRepository;
import com.example.talkingCanvas.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user operations
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapperUtil mapperUtil;

    public UserProfileResponse getUserProfile(Long userId) {
        logger.info("Fetching profile for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapperUtil.toUserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        logger.info("Updating profile for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setName(request.getName());
        user.setContactNumber(request.getContactNumber());

        User updatedUser = userRepository.save(user);
        logger.info("Profile updated successfully for user: {}", userId);
        return mapperUtil.toUserProfileResponse(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        logger.info("Changing password for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed successfully for user: {}", userId);
    }

    public List<OrderResponse> getUserOrders(Long userId, int page, int size) {
        logger.info("Fetching orders for user: {}", userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return orderRepository.findByUserId(userId, pageable)
                .stream()
                .map(order -> mapperUtil.toOrderResponse(order, null))
                .collect(Collectors.toList());
    }
}
