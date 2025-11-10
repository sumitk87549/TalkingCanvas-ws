package com.example.talkingCanvas.config;

import com.example.talkingCanvas.model.*;
import com.example.talkingCanvas.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Initializes database with default admin user and sample data
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PaintingCategoryRepository categoryRepository;
    private final PaintingRepository paintingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Value("${admin.default.name}")
    private String adminName;

    @Value("${admin.default.uncle.name}")
    private String defaultArtistName;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting data initialization...");

        // Create default admin user if not exists
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .name(adminName)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .contactNumber("+911234567890")
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();

            Address adminAddress = Address.builder()
                    .user(admin)
                    .street("Admin Office")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .country("India")
                    .pincode("400001")
                    .isDefault(true)
                    .build();

            admin.getAddresses().add(adminAddress);

            Cart adminCart = Cart.builder()
                    .user(admin)
                    .build();
            admin.setCart(adminCart);

            userRepository.save(admin);
            logger.info("Default admin user created: {}", adminEmail);
        } else {
            logger.info("Admin user already exists");
        }

        // Create default categories
        createDefaultCategories();

        // Create sample paintings
        createSamplePaintings();

        logger.info("Data initialization completed");
    }

    private void createDefaultCategories() {
        List<String> categories = Arrays.asList(
                "Abstract",
                "Landscape",
                "Portrait",
                "Modern",
                "Contemporary",
                "Impressionist",
                "Expressionist",
                "Realism",
                "Still Life",
                "Nature"
        );

        for (String categoryName : categories) {
            if (!categoryRepository.existsByName(categoryName)) {
                PaintingCategory category = PaintingCategory.builder()
                        .name(categoryName)
                        .description("Collection of " + categoryName.toLowerCase() + " oil paintings")
                        .build();
                categoryRepository.save(category);
                logger.info("Category created: {}", categoryName);
            }
        }
    }

    private void createSamplePaintings() {
        if (paintingRepository.count() > 0) {
            logger.info("Sample paintings already exist");
            return;
        }

        // Get categories
        PaintingCategory abstract = categoryRepository.findByName("Abstract").orElse(null);
        PaintingCategory landscape = categoryRepository.findByName("Landscape").orElse(null);
        PaintingCategory modern = categoryRepository.findByName("Modern").orElse(null);

        // Sample Painting 1
        Painting painting1 = Painting.builder()
                .title("Sunset Over Mountains")
                .description("A breathtaking oil painting capturing the golden hues of sunset cascading over majestic mountain peaks. " +
                        "This piece evokes a sense of tranquility and natural beauty, perfect for creating a serene atmosphere in any space.")
                .artistName(defaultArtistName)
                .price(new BigDecimal("45000.00"))
                .currency("INR")
                .height(60.0)
                .width(90.0)
                .depth(2.0)
                .medium("Oil on Canvas")
                .yearCreated(2023)
                .isAvailable(true)
                .stockQuantity(1)
                .adminRecommendation(true)
                .recommendationText("A masterpiece that captures the ethereal beauty of nature's transition. Highly recommended for collectors.")
                .seoTitle("Sunset Over Mountains - Original Oil Painting by " + defaultArtistName)
                .seoDescription("Buy authentic oil painting depicting stunning sunset over mountains. Hand-painted by renowned artist " + defaultArtistName + ". Free shipping across India.")
                .seoKeywords("sunset painting, mountain landscape, oil on canvas, Indian artist, contemporary art")
                .build();

        if (landscape != null) painting1.getCategories().add(landscape);
        if (modern != null) painting1.getCategories().add(modern);
        paintingRepository.save(painting1);
        logger.info("Sample painting 1 created");

        // Sample Painting 2
        Painting painting2 = Painting.builder()
                .title("Abstract Emotions")
                .description("An expressive abstract composition featuring bold brushstrokes and vibrant colors that evoke deep emotional responses. " +
                        "This contemporary piece adds a dynamic focal point to modern interiors.")
                .artistName(defaultArtistName)
                .price(new BigDecimal("35000.00"))
                .currency("INR")
                .height(75.0)
                .width(75.0)
                .depth(2.5)
                .medium("Oil on Canvas")
                .yearCreated(2024)
                .isAvailable(true)
                .stockQuantity(2)
                .adminRecommendation(false)
                .seoTitle("Abstract Emotions - Modern Oil Painting")
                .seoDescription("Contemporary abstract oil painting with vibrant colors and expressive brushwork. Perfect for modern homes and offices.")
                .seoKeywords("abstract art, contemporary painting, modern decor, oil painting India")
                .build();

        if (abstract != null) painting2.getCategories().add(abstract);
        if (modern != null) painting2.getCategories().add(modern);
        paintingRepository.save(painting2);
        logger.info("Sample painting 2 created");

        // Sample Painting 3
        Painting painting3 = Painting.builder()
                .title("Serene Lake Reflection")
                .description("A peaceful landscape depicting a crystal-clear lake reflecting surrounding trees and sky. " +
                        "The delicate play of light and shadow creates a mesmerizing effect that brings nature's calm into your living space.")
                .artistName(defaultArtistName)
                .price(new BigDecimal("52000.00"))
                .currency("INR")
                .height(50.0)
                .width(100.0)
                .depth(2.0)
                .medium("Oil on Canvas")
                .yearCreated(2023)
                .isAvailable(true)
                .stockQuantity(1)
                .adminRecommendation(true)
                .recommendationText("An exquisite representation of nature's serenity. One of our most sought-after pieces.")
                .seoTitle("Serene Lake Reflection - Landscape Oil Painting")
                .seoDescription("Beautiful lake landscape oil painting with stunning reflections. Original artwork by master artist. Ships nationwide.")
                .seoKeywords("lake painting, landscape art, nature painting, oil on canvas, home decor")
                .build();

        if (landscape != null) painting3.getCategories().add(landscape);
        paintingRepository.save(painting3);
        logger.info("Sample painting 3 created");
    }
}
