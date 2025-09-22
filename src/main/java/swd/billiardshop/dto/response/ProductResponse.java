package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Integer productId;
    private Integer categoryId;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String originCountry;
    private Integer warrantyPeriod;
    private String weightRange;
    private String tipSize;
    private String material;
    private String brand;
    private String sku;
    private BigDecimal price;
    private BigDecimal comparePrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private BigDecimal weight;
    private String dimensions;
    private String status;
    private Boolean isFeatured;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer viewCount;
    private Integer salesCount;
    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
