package swd.billiardshop.dto.request;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UserLoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
