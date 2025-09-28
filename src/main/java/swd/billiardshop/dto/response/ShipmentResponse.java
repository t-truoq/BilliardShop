package swd.billiardshop.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShipmentResponse {
    private String trackingCode;
    private String carrier;
    private String status;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
}
