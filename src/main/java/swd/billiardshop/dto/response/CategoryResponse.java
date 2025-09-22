package swd.billiardshop.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private Integer categoryId;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private String imagePublicId;
    private Integer parentId;
    private Integer sortOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
