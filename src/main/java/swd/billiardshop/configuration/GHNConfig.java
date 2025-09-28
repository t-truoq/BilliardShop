package swd.billiardshop.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Configuration
@Data
@ConfigurationProperties(prefix = "ghn")
public class GHNConfig {
    private String apiUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api";
    private String token;
    private Integer shopId;
    private String shopPhone;
    private String shopAddress;
    private Integer shopWardId;
    private Integer shopDistrictId;
}
