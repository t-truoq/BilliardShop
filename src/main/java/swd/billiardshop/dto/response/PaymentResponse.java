package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentResponse {
    private String paymentCode;
    private Integer orderId;
    private BigDecimal amount;
    private String method;
    private String status;
}
