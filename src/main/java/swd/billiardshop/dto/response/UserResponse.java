package swd.billiardshop.dto.response;

import lombok.Data;
import swd.billiardshop.enums.Gender;
import swd.billiardshop.enums.Role;
import swd.billiardshop.enums.Status;

@Data
public class UserResponse {
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Gender gender;
    private java.time.LocalDate dateOfBirth;
    private String avatarUrl;
    private Role role;
    private Status status;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime lastLoginAt;
    private java.time.LocalDateTime emailVerifiedAt;
}
