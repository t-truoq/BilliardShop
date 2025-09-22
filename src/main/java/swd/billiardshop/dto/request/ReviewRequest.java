package swd.billiardshop.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ReviewRequest {
    @NotNull
    private Integer productId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating; // 1-5

    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String comment;
}
