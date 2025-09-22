package swd.billiardshop.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressResponse {
    private Integer addressId;
    private String recipientName;
    private String phone;
    private String addressLine;
    private String ward;
    private String district;
    private String city;
    private String province;
    private String postalCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
