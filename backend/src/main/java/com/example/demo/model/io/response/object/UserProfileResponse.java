package com.example.demo.model.io.response.object;

import com.example.demo.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private LocalDate birthdate;
    private Gender gender;
    private String email;
    private String avatarUrl;
    private Boolean isVerified;
    private Integer vipDaysLeft;
    private UserStatsResponse stats;
}