package com.example.demo.model.io.response.object;

import com.example.demo.model.enums.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitedUserResponse {
    private Long userId;
    private String username;
    private String avatarUrl;
    private Permission permission;
}