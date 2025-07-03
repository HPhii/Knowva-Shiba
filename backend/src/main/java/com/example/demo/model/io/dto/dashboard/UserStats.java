package com.example.demo.model.io.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStats {
    private long totalUsers;
    private long newUsersLast7Days;
    private long activeUsersLast7Days;
}