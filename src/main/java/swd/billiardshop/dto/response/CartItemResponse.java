package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private Integer itemId;
    private Integer productId;
    private String productName;
    private String productSlug;
    private String productImage;
    private String productSku;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
    private Integer availableStock;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
}
