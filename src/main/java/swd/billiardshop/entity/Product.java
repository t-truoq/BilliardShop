package swd.billiardshop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import swd.billiardshop.enums.ProductStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 200, unique = true, nullable = false)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String originCountry;

    private Integer warrantyPeriod;

    @Column(length = 50)
    private String weightRange;

    @Column(length = 20)
    private String tipSize;

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String shortDescription;

    @Column(length = 100, unique = true, nullable = false)
    private String sku;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(precision = 15, scale = 2)
    private BigDecimal comparePrice;

    @Builder.Default
    private Integer stockQuantity = 0;

    @Builder.Default
    private Integer minStockLevel = 0;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(length = 50)
    private String dimensions;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Builder.Default
    private Boolean isFeatured = false;

    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Builder.Default
    private Integer reviewCount = 0;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer salesCount = 0;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
