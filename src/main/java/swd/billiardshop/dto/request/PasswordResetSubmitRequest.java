package swd.billiardshop.dto.request;

import lombok.Data;

@Data
public class PasswordResetSubmitRequest {
    private String email;
    private String token;
    private String newPassword;
}

