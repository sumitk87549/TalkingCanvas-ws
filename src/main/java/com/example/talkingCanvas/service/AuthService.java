package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.auth.AuthResponse;
import com.example.talkingCanvas.dto.auth.LoginRequest;
import com.example.talkingCanvas.dto.auth.RegisterRequest;
import com.example.talkingCanvas.exception.BadRequestException;
import com.example.talkingCanvas.model.Address;
import com.example.talkingCanvas.model.Cart;
import com.example.talkingCanvas.model.User;
import com.example.talkingCanvas.repository.UserRepository;
import com.example.talkingCanvas.security.JwtTokenProvider;
import com.example.talkingCanvas.util.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .contactNumber(request.getContactNumber())
                .role(User.Role.USER)
                .isActive(true)
                .build();

        // Create default address
        Address address = Address.builder()
                .user(user)
                .street(request.getStreet() != null ? request.getStreet() : "")
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .pincode(request.getPincode())
                .isDefault(true)
                .build();

        user.getAddresses().add(address);

        // Create cart for user
        Cart cart = Cart.builder()
                .user(user)
                .build();
        user.setCart(cart);

        // Save user
        User savedUser = userRepository.save(user);

        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

        // Generate JWT token
        String token = tokenProvider.generateTokenFromUserId(savedUser.getId());

        logger.info("User registered successfully: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .expiresIn(tokenProvider.getExpirationMs())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("User login attempt: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        logger.info("User logged in successfully: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .expiresIn(tokenProvider.getExpirationMs())
                .build();
    }
}
