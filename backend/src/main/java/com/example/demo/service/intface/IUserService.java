package com.example.demo.service.intface;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Status;
import com.example.demo.model.io.dto.UpdateUserProfileDTO;
import com.example.demo.model.io.response.object.UserProfileResponse;
import com.example.demo.model.io.response.paged.PagedUsersResponse;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    PagedUsersResponse getAllUsers(String username, String email, Role role, Status status, Boolean isVerified, Pageable pageable);
    UserProfileResponse getUserProfile(Long id);
    User deleteUser(Long id);
    UpdateUserProfileDTO updateUserProfile(Long id, UpdateUserProfileDTO updateUserProfileDTO);
}