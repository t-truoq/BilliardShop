package swd.billiardshop.dto.request;

import lombok.Data;

@Data
public class EmailVerifySubmitRequest {
    private String email;
    private String token;
}

