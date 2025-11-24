package com.example.talkingCanvas.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Address entity for user delivery addresses
 */
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @EqualsAndHashCode.Exclude
    private User user;

    // @NotBlank(message = "Street address is required")
    // @Column(nullable = false)
    @Column(nullable = true)
    private String street;

    // @NotBlank(message = "City is required")
    // @Column(nullable = false)
    @Column(nullable = true)
    private String city;

    @Column(name = "state")
    private String state;

    // @NotBlank(message = "Country is required")
    // @Column(nullable = false)
    @Column(nullable = true)
    private String country;

    @Column(name = "pincode", nullable = true)
    private String pincode;

    @Column(name = "is_default", columnDefinition = "boolean default false", nullable=false)
    @Builder.Default
    private Boolean isDefault = false;
}
