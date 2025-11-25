package com.example.talkingCanvas.service;

import com.example.talkingCanvas.dto.common.PageResponse;
import com.example.talkingCanvas.dto.painting.CategoryDTO;
import com.example.talkingCanvas.dto.painting.CreatePaintingRequest;
import com.example.talkingCanvas.dto.painting.PaintingResponse;
import com.example.talkingCanvas.exception.ResourceNotFoundException;
import com.example.talkingCanvas.model.Painting;
import com.example.talkingCanvas.model.PaintingCategory;
import com.example.talkingCanvas.model.PaintingCertificate;
import com.example.talkingCanvas.model.PaintingImage;
import com.example.talkingCanvas.repository.*;
import com.example.talkingCanvas.util.FileStorageService;
import com.example.talkingCanvas.util.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for painting operations
 */
@Service
@RequiredArgsConstructor
public class PaintingService {

    private static final Logger logger = LoggerFactory.getLogger(PaintingService.class);

    private final PaintingRepository paintingRepository;
    private final PaintingCategoryRepository categoryRepository;
    private final PaintingImageRepository imageRepository;
    private final PaintingCertificateRepository certificateRepository;
    private final FileStorageService fileStorageService;
    private final MapperUtil mapperUtil;

    @Value("${admin.default.uncle.name}")
    private String defaultArtistName;

    @Transactional(readOnly = true)
    public PageResponse<PaintingResponse> getAllPaintings(int page, int size, String sortBy, String sortDirection) {
        logger.info("Fetching all available paintings - page: {}, size: {}", page, size);
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Painting> paintingPage = paintingRepository.findByIsAvailable(true, pageable);
        return mapToPageResponse(paintingPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaintingResponse> getAllPaintingsForAdmin(int page, int size, String sortBy,
            String sortDirection) {
        logger.info("Fetching all paintings for admin - page: {}, size: {}", page, size);
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Painting> paintingPage = paintingRepository.findAll(pageable);
        return mapToPageResponse(paintingPage);
    }

    @Transactional
    public PaintingResponse getPaintingById(Long id) {
        logger.info("Fetching painting: {}", id);
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", id));

        // Increment view count
        painting.incrementViewCount();
        paintingRepository.save(painting);

        return mapperUtil.toPaintingResponse(painting);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaintingResponse> getFeaturedPaintings(int page, int size) {
        logger.info("Fetching featured paintings");
        Pageable pageable = PageRequest.of(page, size);
        Page<Painting> paintingPage = paintingRepository.findByAdminRecommendationAndIsAvailable(true, true, pageable);
        return mapToPageResponse(paintingPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaintingResponse> searchPaintings(String query, int page, int size) {
        logger.info("Searching paintings with query: {}", query);
        Pageable pageable = PageRequest.of(page, size);
        Page<Painting> paintingPage = paintingRepository.searchPaintings(query, pageable);
        return mapToPageResponse(paintingPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<PaintingResponse> filterByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page,
            int size) {
        logger.info("Filtering paintings by price range: {} - {}", minPrice, maxPrice);
        Pageable pageable = PageRequest.of(page, size);
        Page<Painting> paintingPage = paintingRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return mapToPageResponse(paintingPage);
    }

    @Transactional
    public PaintingResponse createPainting(CreatePaintingRequest request) {
        logger.info("Creating new painting: {}", request.getTitle());

        Painting painting = Painting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .artistName(request.getArtistName() != null ? request.getArtistName() : defaultArtistName)
                .price(request.getPrice())
                .currency(request.getCurrency())
                .height(request.getHeight())
                .width(request.getWidth())
                .depth(request.getDepth())
                .medium(request.getMedium())
                .yearCreated(request.getYearCreated())
                .isAvailable(request.getIsAvailable())
                .stockQuantity(request.getStockQuantity())
                .adminRecommendation(request.getAdminRecommendation())
                .recommendationText(request.getRecommendationText())
                .seoTitle(request.getSeoTitle())
                .seoDescription(request.getSeoDescription())
                .seoKeywords(request.getSeoKeywords())
                .build();

        // Add categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            for (Long categoryId : request.getCategoryIds()) {
                PaintingCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
                painting.getCategories().add(category);
            }
        }

        Painting savedPainting = paintingRepository.save(painting);
        logger.info("Painting created successfully: {}", savedPainting.getId());
        return mapperUtil.toPaintingResponse(savedPainting);
    }

    @Transactional
    public PaintingResponse createPaintingWithImages(CreatePaintingRequest request, List<String> imageUrls) {
        logger.info(
                "createPaintingWithImages is deprecated for URL-based images. Falling back to createPainting() without images for request: {}",
                request.getTitle());
        return createPainting(request);
    }

    @Transactional
    public List<PaintingResponse> createPaintingsWithImages(List<CreatePaintingRequest> requests,
            List<List<String>> imageUrlLists) {
        logger.info(
                "createPaintingsWithImages is deprecated for URL-based images. Falling back to createPainting() for each request");
        return requests.stream().map(this::createPainting).collect(Collectors.toList());
    }

    @Transactional
    public PaintingResponse updatePainting(Long id, CreatePaintingRequest request) {
        logger.info("Updating painting: {}", id);
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", id));

        painting.setTitle(request.getTitle());
        painting.setDescription(request.getDescription());
        painting.setArtistName(request.getArtistName() != null ? request.getArtistName() : defaultArtistName);
        painting.setPrice(request.getPrice());
        painting.setCurrency(request.getCurrency());
        painting.setHeight(request.getHeight());
        painting.setWidth(request.getWidth());
        painting.setDepth(request.getDepth());
        painting.setMedium(request.getMedium());
        painting.setYearCreated(request.getYearCreated());
        painting.setIsAvailable(request.getIsAvailable());
        painting.setStockQuantity(request.getStockQuantity());
        painting.setAdminRecommendation(request.getAdminRecommendation());
        painting.setRecommendationText(request.getRecommendationText());
        painting.setSeoTitle(request.getSeoTitle());
        painting.setSeoDescription(request.getSeoDescription());
        painting.setSeoKeywords(request.getSeoKeywords());

        // Update categories
        if (request.getCategoryIds() != null) {
            painting.getCategories().clear();
            for (Long categoryId : request.getCategoryIds()) {
                PaintingCategory category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));
                painting.getCategories().add(category);
            }
        }

        Painting updatedPainting = paintingRepository.save(painting);
        logger.info("Painting updated successfully: {}", id);
        return mapperUtil.toPaintingResponse(updatedPainting);
    }

    @Transactional
    public void deletePainting(Long id) {
        logger.info("Soft deleting painting: {}", id);
        Painting painting = paintingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", id));
        painting.setIsAvailable(false);
        paintingRepository.save(painting);
        logger.info("Painting soft deleted: {}", id);
    }

    @Transactional
    public void uploadImages(Long paintingId, MultipartFile[] files) {
        logger.info("Uploading {} images for painting: {}", files.length, paintingId);
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", paintingId));

        boolean hasPrimaryImage = !painting.getImages().isEmpty();
        int displayOrder = painting.getImages().size();

        for (MultipartFile file : files) {
            try {
                PaintingImage image = PaintingImage.builder()
                        .painting(painting)
                        .data(file.getBytes())
                        .fileName(file.getOriginalFilename())
                        .contentType(file.getContentType())
                        .size(file.getSize())
                        .displayOrder(displayOrder++)
                        .isPrimary(!hasPrimaryImage)
                        .build();
                painting.getImages().add(image);
                hasPrimaryImage = true;
            } catch (Exception ex) {
                logger.error("Failed to process image file for painting {}: {}", paintingId, ex.getMessage());
                throw new RuntimeException("Failed to upload images", ex);
            }
        }

        paintingRepository.save(painting);
        logger.info("Images uploaded successfully for painting: {}", paintingId);
    }

    @Transactional
    public void deleteImage(Long paintingId, Long imageId) {
        logger.info("Deleting image {} from painting {}", imageId, paintingId);
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", paintingId));

        PaintingImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", imageId));

        if (!image.getPainting().getId().equals(paintingId)) {
            throw new IllegalArgumentException("Image does not belong to the specified painting");
        }

        painting.getImages().remove(image);
        imageRepository.delete(image);

        // If primary image was deleted, set a new primary image if available
        if (Boolean.TRUE.equals(image.getIsPrimary()) && !painting.getImages().isEmpty()) {
            painting.getImages().get(0).setIsPrimary(true);
        }

        paintingRepository.save(painting);
        logger.info("Image deleted successfully");
    }

    @Transactional
    public void uploadCertificate(Long paintingId, MultipartFile file, String title, String issuer, LocalDate issueDate,
            String description) {
        logger.info("Uploading certificate for painting: {}", paintingId);
        Painting painting = paintingRepository.findById(paintingId)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", paintingId));

        String filePath = fileStorageService.storeFile(file, "certificates");
        PaintingCertificate certificate = PaintingCertificate.builder()
                .painting(painting)
                .certificateUrl(filePath)
                .title(title)
                .issuer(issuer)
                .issueDate(issueDate)
                .description(description)
                .build();

        certificateRepository.save(certificate);
        logger.info("Certificate uploaded successfully for painting: {}", paintingId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        logger.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(mapperUtil::toCategoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO createCategory(String name, String description) {
        logger.info("Creating new category: {}", name);
        PaintingCategory category = PaintingCategory.builder()
                .name(name)
                .description(description)
                .build();
        PaintingCategory savedCategory = categoryRepository.save(category);
        return mapperUtil.toCategoryDTO(savedCategory);
    }

    // Additional utility methods for painting and image operations
    @Transactional(readOnly = true)
    public PaintingResponse getPaintingWithImages(Long id) {
        logger.info("Fetching painting with images: {}", id);
        Painting painting = paintingRepository.findByIdWithImages(id)
                .orElseThrow(() -> new ResourceNotFoundException("Painting", "id", id));

        // Increment view count
        painting.incrementViewCount();
        paintingRepository.save(painting);

        return mapperUtil.toPaintingResponse(painting);
    }

    @Transactional
    public PaintingResponse updatePaintingWithImages(Long id, CreatePaintingRequest request, List<String> imageUrls) {
        logger.info(
                "updatePaintingWithImages is deprecated for URL-based images. Falling back to updatePainting() without images for id: {}",
                id);
        return updatePainting(id, request);
    }

    private PageResponse<PaintingResponse> mapToPageResponse(Page<Painting> paintingPage) {
        List<PaintingResponse> content = paintingPage.getContent().stream()
                .map(mapperUtil::toPaintingResponse)
                .collect(Collectors.toList());

        return PageResponse.<PaintingResponse>builder()
                .content(content)
                .pageNumber(paintingPage.getNumber())
                .pageSize(paintingPage.getSize())
                .totalElements(paintingPage.getTotalElements())
                .totalPages(paintingPage.getTotalPages())
                .last(paintingPage.isLast())
                .first(paintingPage.isFirst())
                .build();
    }
}
