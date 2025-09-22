package swd.billiardshop.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductSearchRequest {
    private String q;
    private Integer categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String brand;
    private String material;
    private Integer minRating;
    private Boolean inStockOnly;
    private String sortBy; // price_asc, price_desc, rating, sales, newest
    private Integer page;
    private Integer size;
}
