package swd.billiardshop.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GHNCreateOrderResponse {
    @JsonProperty("order_code")
    private String orderCode;
    @JsonProperty("total_fee")
    private Long totalFee;
    @JsonProperty("expected_delivery_time")
    private String expectedDeliveryTime;

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public Long getTotalFee() { return totalFee; }
    public void setTotalFee(Long totalFee) { this.totalFee = totalFee; }
    public String getExpectedDeliveryTime() { return expectedDeliveryTime; }
    public void setExpectedDeliveryTime(String expectedDeliveryTime) { this.expectedDeliveryTime = expectedDeliveryTime; }
}
