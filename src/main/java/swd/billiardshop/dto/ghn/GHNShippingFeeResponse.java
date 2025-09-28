package swd.billiardshop.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GHNShippingFeeResponse {
    @JsonProperty("total")
    private Long total;

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
}
