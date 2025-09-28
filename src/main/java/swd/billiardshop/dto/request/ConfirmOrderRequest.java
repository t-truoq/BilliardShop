package swd.billiardshop.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmOrderRequest {
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // "momo", "vnpay", "cash", etc.
}
