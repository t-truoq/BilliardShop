package swd.billiardshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderPreviewRequest {
    @NotNull(message = "Address ID không được để trống")
    private Integer addressId;

    private String promotionCode;

    @NotBlank(message = "Phương thức vận chuyển không được để trống")
    private String shippingMethod;

    private java.util.List<SelectedCartItem> selectedItems;
}
