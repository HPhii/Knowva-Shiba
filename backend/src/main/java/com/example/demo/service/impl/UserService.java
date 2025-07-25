package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.model.enums.Status;
import com.example.demo.model.io.dto.UpdateUserProfileDTO;
import com.example.demo.model.io.response.object.UserProfileResponse;
import com.example.demo.model.io.response.paged.PagedUsersResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IUserService;
import com.example.demo.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    @Cacheable(value = "users", key = "#username + '-' + #email + '-' + #role + '-' + #status + '-' + #isVerified + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public PagedUsersResponse getAllUsers(String username, String email, Role role, Status status, Boolean isVerified, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        if (username != null && !username.isBlank()) {
            spec = spec.and(UserSpecification.withUsername(username));
        }
        if (email != null && !email.isBlank()) {
            spec = spec.and(UserSpecification.withEmail(email));
        }
        if (role != null) {
            spec = spec.and(UserSpecification.withRole(role));
        }
        if (status != null) {
            spec = spec.and(UserSpecification.withStatus(status));
        }
        if (isVerified != null) {
            spec = spec.and(UserSpecification.withVerified(isVerified));
        }

        Page<User> userPage = userRepository.findAll(spec, pageable);
        List<User> users = userPage.getContent();

        return new PagedUsersResponse(
                users,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                pageable.getPageNumber()
        );
    }

    @Cacheable(value = "userProfile", key = "#id")
    public UserProfileResponse getUserProfile(Long id) {
        log.info("Fetching user profile for id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Integer vipDaysLeft = null;
        if (user.getAccount().getVipEndDate() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endDate = user.getAccount().getVipEndDate();
            if (endDate.isAfter(now)) {
                vipDaysLeft = (int) ChronoUnit.DAYS.between(now, endDate);
            }
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getAccount().getEmail())
                .fullName(user.getFullName())
                .birthdate(user.getBirthdate())
                .phoneNumber(user.getPhoneNumber())
                .gender(user.getGender())
                .avatarUrl(user.getAvatarUrl())
                .vipDaysLeft(vipDaysLeft)
                .build();
    }

    @Override
    @CacheEvict(value = {"userProfile", "users"}, allEntries = true)
    public User deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.getAccount().setStatus(Status.INACTIVE);
        return userRepository.save(user);
    }

    @Override
    @CacheEvict(value = {"userProfile", "users"}, allEntries = true)
    public UpdateUserProfileDTO updateUserProfile(Long id, UpdateUserProfileDTO updateUserProfileDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (updateUserProfileDTO.getFullName() != null) user.setFullName(updateUserProfileDTO.getFullName());
        if (updateUserProfileDTO.getPhoneNumber() != null) user.setPhoneNumber(updateUserProfileDTO.getPhoneNumber());
        if (updateUserProfileDTO.getBirthdate() != null) user.setBirthdate(updateUserProfileDTO.getBirthdate());
        if (updateUserProfileDTO.getGender() != null) user.setGender(updateUserProfileDTO.getGender());
        if (updateUserProfileDTO.getUsername() != null) user.getAccount().setUsername(updateUserProfileDTO.getUsername());
        if (updateUserProfileDTO.getEmail() != null) user.getAccount().setEmail(updateUserProfileDTO.getEmail());
        if (updateUserProfileDTO.getAvatarUrl() != null) user.setAvatarUrl(updateUserProfileDTO.getAvatarUrl());

        userRepository.save(user);

        return UpdateUserProfileDTO.builder()
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .birthdate(user.getBirthdate())
                .gender(user.getGender())
                .username(user.getAccount().getUsername())
                .email(user.getAccount().getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}