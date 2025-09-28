package swd.billiardshop.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class GHNCreateOrderRequest {
    @JsonProperty("from_name")
    private String fromName;
    @JsonProperty("from_phone")
    private String fromPhone;
    @JsonProperty("from_address")
    private String fromAddress;
    @JsonProperty("to_name")
    private String toName;
    @JsonProperty("to_phone")
    private String toPhone;
    @JsonProperty("to_address")
    private String toAddress;
    @JsonProperty("to_ward_code")
    private String toWardCode;
    @JsonProperty("to_district_id")
    private Integer toDistrictId;
    @JsonProperty("cod_amount")
    private Long codAmount;
    @JsonProperty("content")
    private String content;
    @JsonProperty("weight")
    private Integer weight;
    @JsonProperty("required_note")
    private String requiredNote;
    @JsonProperty("length")
    private Integer length;
    @JsonProperty("width")
    private Integer width;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("service_type_id")
    private Integer serviceTypeId;
    @JsonProperty("service_id")
    private Integer serviceId;
    @JsonProperty("payment_type_id")
    private Integer paymentTypeId;
    @JsonProperty("client_order_code")
    private String clientOrderCode;
    @JsonProperty("items")
    private List<Object> items;

    public String getFromName() { return fromName; }
    public void setFromName(String fromName) { this.fromName = fromName; }
    public String getFromPhone() { return fromPhone; }
    public void setFromPhone(String fromPhone) { this.fromPhone = fromPhone; }
    public String getFromAddress() { return fromAddress; }
    public void setFromAddress(String fromAddress) { this.fromAddress = fromAddress; }
    public String getToName() { return toName; }
    public void setToName(String toName) { this.toName = toName; }
    public String getToPhone() { return toPhone; }
    public void setToPhone(String toPhone) { this.toPhone = toPhone; }
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }
    public String getToWardCode() { return toWardCode; }
    public void setToWardCode(String toWardCode) { this.toWardCode = toWardCode; }
    public Integer getToDistrictId() { return toDistrictId; }
    public void setToDistrictId(Integer toDistrictId) { this.toDistrictId = toDistrictId; }
    public Long getCodAmount() { return codAmount; }
    public void setCodAmount(Long codAmount) { this.codAmount = codAmount; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }
    public String getRequiredNote() { return requiredNote; }
    public void setRequiredNote(String requiredNote) { this.requiredNote = requiredNote; }
    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    public Integer getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Integer serviceTypeId) { this.serviceTypeId = serviceTypeId; }
    public Integer getServiceId() { return serviceId; }
    public void setServiceId(Integer serviceId) { this.serviceId = serviceId; }
    public Integer getPaymentTypeId() { return paymentTypeId; }
    public void setPaymentTypeId(Integer paymentTypeId) { this.paymentTypeId = paymentTypeId; }
    public String getClientOrderCode() { return clientOrderCode; }
    public void setClientOrderCode(String clientOrderCode) { this.clientOrderCode = clientOrderCode; }
    public List<Object> getItems() { return items; }
    public void setItems(List<Object> items) { this.items = items; }
}
