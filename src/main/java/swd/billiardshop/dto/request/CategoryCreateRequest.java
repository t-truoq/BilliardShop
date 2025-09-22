package swd.billiardshop.dto.request;

import lombok.*;

import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateRequest {
    private String name;
    private String description;
    private Integer parentId;
    private Integer sortOrder;
    private Boolean isActive;
    private MultipartFile image;

    // Not sent by client; available in responses
    private String imagePublicId;
}
