package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartResponse {
    private Integer cartId;
    private Integer userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private LocalDateTime updatedAt;
}
