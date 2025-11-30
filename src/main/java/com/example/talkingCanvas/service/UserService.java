package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.order.OrderResponse;
import com.example.talkingCanvas.dto.user.AddressDTO;
import com.example.talkingCanvas.dto.user.ChangePasswordRequest;
import com.example.talkingCanvas.dto.user.UpdateProfileRequest;
import com.example.talkingCanvas.dto.user.UserProfileResponse;
import com.example.talkingCanvas.exception.BadRequestException;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.Address;
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

        // Update profile emoji if provided
        if (request.getProfileEmoji() != null && !request.getProfileEmoji().isEmpty()) {
            user.setProfileEmoji(request.getProfileEmoji());
        }

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

    // Address Management Methods
    public List<AddressDTO> getUserAddresses(Long userId) {
        logger.info("Fetching addresses for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return user.getAddresses().stream()
                .map(mapperUtil::toAddressDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AddressDTO addAddress(Long userId, AddressDTO addressDTO) {
        logger.info("Adding new address for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = Address.builder()
                .user(user)
                .street(addressDTO.getStreet())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .country(addressDTO.getCountry())
                .pincode(addressDTO.getPincode())
                .isDefault(addressDTO.getIsDefault() != null && addressDTO.getIsDefault())
                .build();

        // If this is set as default, unset other default addresses
        if (address.getIsDefault()) {
            user.getAddresses().forEach(addr -> addr.setIsDefault(false));
        }

        user.getAddresses().add(address);
        userRepository.save(user);
        logger.info("Address added successfully for user: {}", userId);
        return mapperUtil.toAddressDTO(address);
    }

    @Transactional
    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO addressDTO) {
        logger.info("Updating address: {} for user: {}", addressId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setPincode(addressDTO.getPincode());

        // Handle default flag
        if (addressDTO.getIsDefault() != null && addressDTO.getIsDefault() && !address.getIsDefault()) {
            user.getAddresses().forEach(addr -> addr.setIsDefault(false));
            address.setIsDefault(true);
        }

        userRepository.save(user);
        logger.info("Address updated successfully: {}", addressId);
        return mapperUtil.toAddressDTO(address);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        logger.info("Deleting address: {} for user: {}", addressId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        user.getAddresses().remove(address);
        userRepository.save(user);
        logger.info("Address deleted successfully: {}", addressId);
    }

    @Transactional
    public AddressDTO setDefaultAddress(Long userId, Long addressId) {
        logger.info("Setting default address: {} for user: {}", addressId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Address address = user.getAddresses().stream()
                .filter(addr -> addr.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Unset all default addresses
        user.getAddresses().forEach(addr -> addr.setIsDefault(false));

        // Set this one as default
        address.setIsDefault(true);
        userRepository.save(user);
        logger.info("Default address set successfully: {}", addressId);
        return mapperUtil.toAddressDTO(address);
    }
}
