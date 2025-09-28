package swd.billiardshop.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GHNProvince {
    @JsonProperty("ProvinceID")
    private Integer provinceId;
    @JsonProperty("ProvinceName")
    private String provinceName;
    @JsonProperty("Code")
    private String code;

    public Integer getProvinceId() { return provinceId; }
    public void setProvinceId(Integer provinceId) { this.provinceId = provinceId; }
    public String getProvinceName() { return provinceName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
