package swd.billiardshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.*;

@Data
public class ProductRequest {
    private Integer categoryId;

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Size(max = 200)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers and hyphens")
    private String slug;

    private String description;
    private String shortDescription;
    private String originCountry;
    private Integer warrantyPeriod;
    private String weightRange;
    private String tipSize;
    private String material;
    private String brand;

    @NotBlank
    @Size(max = 100)
    @Pattern(regexp = "^[A-Za-z0-9-_]+$", message = "SKU must be alphanumeric with optional - or _ characters")
    private String sku;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    private BigDecimal price;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal comparePrice;

    @Min(0)
    private Integer stockQuantity;

    @Min(0)
    private Integer minStockLevel;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal weight;

    private String dimensions;
    private Boolean isFeatured;
}
