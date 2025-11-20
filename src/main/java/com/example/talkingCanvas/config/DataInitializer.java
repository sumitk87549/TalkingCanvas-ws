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
import java.util.ArrayList;
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
    private final PaintingImageRepository paintingImageRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.email}")
    private String adminEmail;

    @Value("${admin.default.password}")
    private String adminPassword;

    @Value("${admin.default.name}")
    private String adminName;

    @Value("${admin.default.admin.name}")
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
                    .contactNumber("+919414061556")
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
        }

        // Create default admin2 user if not exists
        if (!userRepository.existsByEmail("sumitk87549@gmail.com")) {
            User adminS = User.builder()
                    .name("Sumit")
                    .email("sumitk87549@gmail.com")
                    .password(passwordEncoder.encode("sumitk87549"))
                    .contactNumber("+917976611437")
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();

            Address adminSAddress = Address.builder()
                    .user(adminS)
                    .street("Pratap Nagar, Mayo Link Road")
                    .city("Ajmer")
                    .state("Rajasthan")
                    .country("India")
                    .pincode("305001")
                    .isDefault(true)
                    .build();

            adminS.getAddresses().add(adminSAddress);

            Cart adminSCart = Cart.builder()
                    .user(adminS)
                    .build();
            adminS.setCart(adminSCart);

            userRepository.save(adminS);
            logger.info("Default admin user created: {}", "sumitk87549@gmail.com");
        }

        // Create default admin user3 if not exists
        if (!userRepository.existsByEmail("praveen_kumar@gmail.com")) {
            User adminP = User.builder()
                    .name("Praveen Kumar")
                    .email("praveen_kumar@gmail.com")
                    .password(passwordEncoder.encode("artiskPK"))
                    .contactNumber("+919999265918")
                    .role(User.Role.ADMIN)
                    .isActive(true)
                    .build();

            Address adminPAddress = Address.builder()
                    .user(adminP)
                    .street("C/O Parwati Devi, Gali no. 15, Tanajinagar, Bhajanganj")
                    .city("Ajmer")
                    .state("Rajasthan")
                    .country("India")
                    .pincode("305001")
                    .isDefault(true)
                    .build();

            adminP.getAddresses().add(adminPAddress);

                    Cart adminPCart = Cart.builder()
                            .user(adminP)
                            .build();
                    adminP.setCart(adminPCart);

                    userRepository.save(adminP);
            logger.info("Default admin user created: {}", "praveen_kumar@gmail.com");
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
        PaintingCategory abstractCategory = categoryRepository.findByName("Abstract").orElse(null);
        PaintingCategory landscape = categoryRepository.findByName("Landscape").orElse(null);
        PaintingCategory modern = categoryRepository.findByName("Modern").orElse(null);

        // Build images first
        PaintingImage image1 = PaintingImage.builder()
                .imageUrl("sunset-mountains.jpg")
                .displayOrder(0)
                .isPrimary(true)
                .build();

        PaintingImage image2 = PaintingImage.builder()
                .imageUrl("abstract-emotions.jpg")
                .displayOrder(0)
                .isPrimary(true)
                .build();

        PaintingImage image3 = PaintingImage.builder()
                .imageUrl("lake-reflection.jpg")
                .displayOrder(0)
                .isPrimary(true)
                .build();

        // Sample Painting 1 with images - uses saveWithImages method
        List<PaintingImage> painting1Images = new ArrayList<>();
        painting1Images.add(image1);

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
                .images(painting1Images)
                .build();

        if (landscape != null) painting1.getCategories().add(landscape);
        if (modern != null) painting1.getCategories().add(modern);

        // Save painting with images using the new method - automatically handles image relationships
        paintingRepository.saveWithImages(painting1);
        logger.info("Sample painting 1 created with image using saveWithImages method");

        // Sample Painting 2
        List<PaintingImage> painting2Images = new ArrayList<>();
        painting2Images.add(image2);

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
                .images(painting2Images)
                .build();

        if (abstractCategory != null) painting2.getCategories().add(abstractCategory);
        if (modern != null) painting2.getCategories().add(modern);

        paintingRepository.saveWithImages(painting2);
        logger.info("Sample painting 2 created with image using saveWithImages method");

        // Sample Painting 3
        List<PaintingImage> painting3Images = new ArrayList<>();
        painting3Images.add(image3);

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
                .images(painting3Images)
                .build();

        if (landscape != null) painting3.getCategories().add(landscape);

        paintingRepository.saveWithImages(painting3);
        logger.info("Sample painting 3 created with image using saveWithImages method");
    }
}
