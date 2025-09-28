package swd.billiardshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Address ID không được để trống")
    private Integer addressId;

    private String notes;

    private String promotionCode;

    @NotBlank(message = "Phương thức vận chuyển không được để trống")
    private String shippingMethod; // e.g. "ghn"
    
    // optional: nếu gửi thì chỉ order các item này (cart item ids + quantities)
    private java.util.List<SelectedCartItem> selectedItems;
}
