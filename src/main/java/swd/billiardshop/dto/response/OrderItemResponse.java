package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Integer itemId;
    private Integer productId;
    private String productName;
    private String productSku;
    private String productImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
