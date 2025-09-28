package swd.billiardshop.dto.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PromotionResponse {
    private Integer promotionId;
    private String code;
    private String title;
    private BigDecimal discountValue;
}
