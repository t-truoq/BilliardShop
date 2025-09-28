package swd.billiardshop.dto.ghn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GHNShippingFeeRequest {
    @JsonProperty("from_district_id")
    private Integer fromDistrictId;

    @JsonProperty("from_ward_code")
    private String fromWardCode;

    @JsonProperty("to_district_id")
    private Integer toDistrictId;

    @JsonProperty("to_ward_code")
    private String toWardCode;

    @JsonProperty("weight")
    private Integer weight;

    @JsonProperty("length")
    private Integer length;

    @JsonProperty("width")
    private Integer width;

    @JsonProperty("height")
    private Integer height;

    @JsonProperty("service_id")
    private Integer serviceId;

    @JsonProperty("service_type_id")
    private Integer serviceTypeId;

    @JsonProperty("insurance_value")
    private Long insuranceValue;

    @JsonProperty("coupon")
    private String coupon;
}
