package com.example.talkingCanvas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private User user;

    @NotBlank(message = "Street address is required")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;

    @Column(name = "state")
    private String state;

    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
