package swd.billiardshop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import swd.billiardshop.configuration.GHNConfig;
import swd.billiardshop.dto.ghn.GHNBaseResponse;
import swd.billiardshop.dto.ghn.GHNShippingFeeRequest;
import swd.billiardshop.dto.ghn.GHNShippingFeeResponse;
import swd.billiardshop.dto.ghn.GHNCreateOrderResponse;
import swd.billiardshop.dto.ghn.GHNProvince;
import swd.billiardshop.dto.ghn.GHNDistrict;
import swd.billiardshop.dto.ghn.GHNWard;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class GHNClientService {
    private static final Logger logger = LoggerFactory.getLogger(GHNClientService.class);

    @Autowired
    private GHNConfig ghnConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnConfig.getToken());
        headers.set("ShopId", String.valueOf(ghnConfig.getShopId()));
        return headers;
    }

    public java.util.List<GHNProvince> getProvinces() {
        try {
            String url = ghnConfig.getApiUrl() + "/master-data/province";
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<java.util.List<GHNProvince>> parsed = objectMapper.readValue(resp.getBody(), new com.fasterxml.jackson.core.type.TypeReference<GHNBaseResponse<java.util.List<GHNProvince>>>(){});
                if (parsed.getCode() == 200) return parsed.getData();
            }
        } catch (Exception e) {
            logger.warn("GHN getProvinces failed", e);
        }
        return java.util.Collections.emptyList();
    }

    public java.util.List<GHNDistrict> getDistricts(Integer provinceId) {
        try {
            String url = ghnConfig.getApiUrl() + "/master-data/district";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"province_id\": " + provinceId + "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<java.util.List<GHNDistrict>> parsed = objectMapper.readValue(resp.getBody(), new com.fasterxml.jackson.core.type.TypeReference<GHNBaseResponse<java.util.List<GHNDistrict>>>(){});
                if (parsed.getCode() == 200) return parsed.getData();
            }
        } catch (Exception e) {
            logger.warn("GHN getDistricts failed", e);
        }
        return java.util.Collections.emptyList();
    }

    public java.util.List<GHNWard> getWards(Integer districtId) {
        try {
            String url = ghnConfig.getApiUrl() + "/master-data/ward";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"district_id\": " + districtId + "}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<java.util.List<GHNWard>> parsed = objectMapper.readValue(resp.getBody(), new com.fasterxml.jackson.core.type.TypeReference<GHNBaseResponse<java.util.List<GHNWard>>>(){});
                if (parsed.getCode() == 200) return parsed.getData();
            }
        } catch (Exception e) {
            logger.warn("GHN getWards failed", e);
        }
        return java.util.Collections.emptyList();
    }

    public GHNShippingFeeResponse calculateShippingFee(GHNShippingFeeRequest request) {
        try {
            String url = ghnConfig.getApiUrl() + "/v2/shipping-order/fee";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = objectMapper.writeValueAsString(request);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<GHNShippingFeeResponse> gh = objectMapper.readValue(resp.getBody(), new TypeReference<GHNBaseResponse<GHNShippingFeeResponse>>(){});
                if (gh.getCode() == 200) return gh.getData();
            }
        } catch (Exception e) {
            logger.warn("GHN fee API failed", e);
        }
        return null;
    }

    public GHNCreateOrderResponse createShippingOrder(Object requestObj) {
        try {
            String url = ghnConfig.getApiUrl() + "/v2/shipping-order/create";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = objectMapper.writeValueAsString(requestObj);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<GHNCreateOrderResponse> gh = objectMapper.readValue(resp.getBody(), new TypeReference<GHNBaseResponse<GHNCreateOrderResponse>>(){});
                if (gh.getCode() == 200) return gh.getData();
            }
        } catch (Exception e) {
            logger.warn("GHN create order failed", e);
        }
        return null;
    }

    public boolean cancelShippingOrder(String orderCode) {
        try {
            String url = ghnConfig.getApiUrl() + "/v2/shipping-order/cancel";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String body = "{\"order_codes\":[\"" + orderCode + "\"]}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return resp.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.warn("GHN cancel order failed", e);
            return false;
        }
    }
    // Thêm các methods này vào GHNClientService class hiện tại

    /**
     * Get order detail from GHN
     */
    public GHNOrderDetailResponse getOrderDetail(String orderCode) {
        try {
            String url = ghnConfig.getApiUrl() + "/v2/shipping-order/detail";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"order_code\":\"" + orderCode + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<GHNOrderDetailResponse.GHNOrderData> parsed = objectMapper.readValue(
                        resp.getBody(),
                        new TypeReference<GHNBaseResponse<GHNOrderDetailResponse.GHNOrderData>>(){}
                );
                if (parsed.getCode() == 200) {
                    GHNOrderDetailResponse response = new GHNOrderDetailResponse();
                    response.setCode(parsed.getCode());
                    response.setMessage(parsed.getMessage());
                    response.setData(parsed.getData());
                    return response;
                }
            }

            logger.warn("GHN order detail API returned non-success response for order: {}", orderCode);
            return null;

        } catch (Exception e) {
            logger.error("Error calling GHN order detail API for order: {}", orderCode, e);
            return null;
        }
    }

    /**
     * Get order tracking info from GHN
     */
    public java.util.List<GHNOrderLog> getOrderTracking(String orderCode) {
        try {
            String url = ghnConfig.getApiUrl() + "/v2/shipping-order/detail";
            HttpHeaders headers = createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\"order_code\":\"" + orderCode + "\"}";
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (resp.getStatusCode() == HttpStatus.OK) {
                GHNBaseResponse<GHNOrderDetailResponse.GHNOrderData> parsed = objectMapper.readValue(
                        resp.getBody(),
                        new TypeReference<GHNBaseResponse<GHNOrderDetailResponse.GHNOrderData>>(){}
                );
                if (parsed.getCode() == 200 && parsed.getData() != null && parsed.getData().getLog() != null) {
                    return parsed.getData().getLog();
                }
            }

            return new java.util.ArrayList<>();

        } catch (Exception e) {
            logger.error("Error calling GHN tracking API for order: {}", orderCode, e);
            return new java.util.ArrayList<>();
        }
    }
@Data
    public static class GHNOrderDetailResponse {
        private Integer code;
        private String message;
        private GHNOrderData data;


@Data
        public static class GHNOrderData {
            @com.fasterxml.jackson.annotation.JsonProperty("order_code")
            private String orderCode;

            @com.fasterxml.jackson.annotation.JsonProperty("sort_code")
            private String sortCode;

            @com.fasterxml.jackson.annotation.JsonProperty("trans_type")
            private String transType;

            @com.fasterxml.jackson.annotation.JsonProperty("status")
            private String status;

            @com.fasterxml.jackson.annotation.JsonProperty("money_total")
            private Long moneyTotal;

            @com.fasterxml.jackson.annotation.JsonProperty("money_fee")
            private Long moneyFee;

            @com.fasterxml.jackson.annotation.JsonProperty("from_name")
            private String fromName;

            @com.fasterxml.jackson.annotation.JsonProperty("from_address")
            private String fromAddress;

            @com.fasterxml.jackson.annotation.JsonProperty("to_name")
            private String toName;

            @com.fasterxml.jackson.annotation.JsonProperty("to_address")
            private String toAddress;

            @com.fasterxml.jackson.annotation.JsonProperty("to_ward_name")
            private String toWardName;

            @com.fasterxml.jackson.annotation.JsonProperty("to_district_name")
            private String toDistrictName;

            @com.fasterxml.jackson.annotation.JsonProperty("to_province_name")
            private String toProvinceName;

            @com.fasterxml.jackson.annotation.JsonProperty("weight")
            private Integer weight;

            @com.fasterxml.jackson.annotation.JsonProperty("cod_amount")
            private Long codAmount;

            @com.fasterxml.jackson.annotation.JsonProperty("content")
            private String content;

            @com.fasterxml.jackson.annotation.JsonProperty("pick_time")
            private String pickTime;

            @com.fasterxml.jackson.annotation.JsonProperty("deliver_time")
            private String deliverTime;

            @com.fasterxml.jackson.annotation.JsonProperty("total_fee")
            private Long totalFee;

            @com.fasterxml.jackson.annotation.JsonProperty("created_date")
            private String createdDate;

            @com.fasterxml.jackson.annotation.JsonProperty("updated_date")
            private String updatedDate;

            @com.fasterxml.jackson.annotation.JsonProperty("note")
            private String note;

            @com.fasterxml.jackson.annotation.JsonProperty("log")
            private java.util.List<GHNOrderLog> log;

        }
    }
@Data
    public static class GHNOrderLog {
        @com.fasterxml.jackson.annotation.JsonProperty("status")
        private String status;

        @com.fasterxml.jackson.annotation.JsonProperty("updated_date")
        private String updatedDate;

        @com.fasterxml.jackson.annotation.JsonProperty("action_at")
        private String actionAt;

        @com.fasterxml.jackson.annotation.JsonProperty("reason_code")
        private String reasonCode;

        @com.fasterxml.jackson.annotation.JsonProperty("reason")
        private String reason;

        @com.fasterxml.jackson.annotation.JsonProperty("hub_id")
        private Integer hubId;

        @com.fasterxml.jackson.annotation.JsonProperty("hub_name")
        private String hubName;

        @com.fasterxml.jackson.annotation.JsonProperty("location")
        private String location;

    }
}
