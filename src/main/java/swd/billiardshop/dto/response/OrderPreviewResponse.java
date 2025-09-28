package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderPreviewResponse {
    private List<OrderItemResponse> items;
    private AddressResponse shippingAddress;
    private String shippingMethod;
    private BigDecimal shippingCost;
    private BigDecimal subtotal;
    private PromotionResponse appliedPromotion;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String estimatedDelivery;
}
