package swd.billiardshop.dto.response;

import lombok.Data;

@Data
public class ProductImageResponse {
    private Integer imageId;
    private String imageUrl;
    private String altText;
    private Boolean isPrimary;
}
