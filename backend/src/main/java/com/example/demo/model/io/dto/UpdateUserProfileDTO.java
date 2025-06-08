package com.example.demo.model.io.dto;

import com.example.demo.model.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UpdateUserProfileDTO {
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "(84|0[3|5|7|8|9])+([0-9]{8})\\b", message = "Invalid phone number format")
    private String phoneNumber;

    @Past(message = "Birthdate must be in the past")
    private LocalDate birthdate;

    private Gender gender;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "Invalid email format")
    private String email;
}
