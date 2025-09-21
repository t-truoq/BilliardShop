package swd.billiardshop.dto.request;

import lombok.Data;
import swd.billiardshop.enums.Gender;

import jakarta.validation.constraints.*;

@Data
public class UserRegisterRequest {
    @NotBlank
    @Size(min = 4, max = 50)
    private String username;
    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String fullName;
    @Pattern(regexp = "^\\d{10,15}$", message = "Invalid phone number")
    private String phone;
    private Gender gender;
    private java.time.LocalDate dateOfBirth;
}
