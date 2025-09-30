package com.example.demo.controller;

import com.example.demo.model.entity.ActivityLog;
import com.example.demo.model.entity.User;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "17. User Activity") // Tạo tag mới cho Swagger
public class ActivityLogController {

    private final IActivityLogService activityLogService;
    private final IAccountService accountService;

    @GetMapping("/my-activities")
    @Operation(summary = "Lấy hoạt động gần đây của tôi", description = "Lấy danh sách các hoạt động gần đây của người dùng đang đăng nhập, có phân trang.")
    public ResponseEntity<Page<ActivityLog>> getMyActivities(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size) {

        User currentUser = accountService.getCurrentAccount().getUser();
        Pageable pageable = PageRequest.of(page, size);
        Page<ActivityLog> activities = activityLogService.getActivitiesForUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(activities);
    }
}
