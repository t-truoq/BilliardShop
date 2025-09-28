package swd.billiardshop.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GHNDistrict {
    @JsonProperty("DistrictID")
    private Integer districtId;
    @JsonProperty("ProvinceID")
    private Integer provinceId;
    @JsonProperty("DistrictName")
    private String districtName;
    @JsonProperty("Code")
    private String code;
    @JsonProperty("Type")
    private Integer type;
    @JsonProperty("SupportType")
    private Integer supportType;

    public Integer getDistrictId() { return districtId; }
    public void setDistrictId(Integer districtId) { this.districtId = districtId; }
    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
    public String getDistrictName() { return districtName; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
