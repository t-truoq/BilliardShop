package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Integer orderId;
    private String orderNumber;
    private Integer userId;
    private String status;
    private String paymentStatus;

    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private List<OrderItemResponse> items;
    private Integer totalItems;

    private String shippingAddress;
    private AddressResponse shippingAddressDetail;

    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    private PromotionResponse appliedPromotion;
    private String shippingMethod;
    private ShipmentResponse shipment;

    private String notes;
    private String adminNotes;

    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
}
