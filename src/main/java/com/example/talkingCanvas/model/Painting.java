package com.example.talkingCanvas.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Painting entity representing oil paintings for sale
 */
@Entity
@Table(name = "paintings", indexes = {
        @Index(name = "idx_painting_available", columnList = "is_available"),
        @Index(name = "idx_painting_price", columnList = "price")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Painting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Artist name is required")
    @Column(name = "artist_name", nullable = false)
    private String artistName;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "INR";

    @Column(nullable = false)
    private Double height; // in cm

    @Column(nullable = false)
    private Double width; // in cm

    @Column(nullable = false)
    private Double depth; // in cm

    // @NotBlank(message = "Medium is required")
    @Column(nullable = false)
    private String medium; // Oil on Canvas, Oil on Board, etc.

    @Column(name = "year_created")
    private Integer yearCreated;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "stock_quantity", nullable = false)
    @Min(value = 1, message = "Stock quantity must be at least 1")
    @Builder.Default
    private Integer stockQuantity = 1;

    @Column(name = "admin_recommendation", nullable = false)
    @Builder.Default
    private Boolean adminRecommendation = false;

    @Column(name = "recommendation_text", columnDefinition = "TEXT")
    private String recommendationText;

    @OneToMany(mappedBy = "painting", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private List<PaintingImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "painting", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private List<PaintingCertificate> certificates = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "painting_category_mapping", joinColumns = @JoinColumn(name = "painting_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private Set<PaintingCategory> categories = new HashSet<>();

    @Column(name = "seo_title")
    private String seoTitle;

    @Column(name = "seo_description", columnDefinition = "TEXT")
    private String seoDescription;

    @Column(name = "seo_keywords")
    private String seoKeywords;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "purchase_count", nullable = false)
    @Builder.Default
    private Long purchaseCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementPurchaseCount() {
        this.purchaseCount++;
    }

    public String getSeoDescription() {
        return this.seoDescription;
    }

    public String getSeoKeywords() {
        return this.seoKeywords;
    }

    public Long getViewCount() {
        return this.viewCount;
    }

    public Long getPurchaseCount() {
        return this.purchaseCount;
    }
}
