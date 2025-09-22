package swd.billiardshop.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryTreeResponse {
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
    @Builder.Default
    private List<CategoryTreeResponse> children = new ArrayList<>();
}
