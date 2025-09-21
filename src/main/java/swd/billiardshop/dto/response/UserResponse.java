package swd.billiardshop.dto.response;

import lombok.Data;
import swd.billiardshop.enums.Gender;

@Data
public class UserResponse {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Gender gender;
    private java.time.LocalDate dateOfBirth;
}
