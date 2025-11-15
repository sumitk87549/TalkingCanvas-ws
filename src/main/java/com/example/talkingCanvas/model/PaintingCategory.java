package com.example.talkingCanvas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing painting categories/styles
 */
@Entity
@Table(name = "painting_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaintingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToMany(mappedBy = "categories")
    @Builder.Default
    @EqualsAndHashCode.Exclude
    private Set<Painting> paintings = new HashSet<>();
}
