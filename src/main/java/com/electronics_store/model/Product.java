package com.electronics_store.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 15, scale = 2)
    // 15 số cả phần thập phân, 2 số thập phân sau dấu chấm
    private BigDecimal basePrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "rating_avg")
    private Double ratingAvg = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "rating_1")
    private Integer rating1 = 0;

    @Column(name = "rating_2")
    private Integer rating2 = 0;

    @Column(name = "rating_3")
    private Integer rating3 = 0;

    @Column(name = "rating_4")
    private Integer rating4 = 0;

    @Column(name = "rating_5")
    private Integer rating5 = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private java.util.Date updatedAt;

    /* ---------- IMAGES ---------- */
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY
    )
    private List<ProductImage> images = new ArrayList<>();

    /* ---------- VARIANTS ---------- */
    @OneToMany(
            mappedBy = "product",
            fetch = FetchType.LAZY
    )
    private List<ProductVariant> variants = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
        updatedAt = new java.util.Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new java.util.Date();
    }
}