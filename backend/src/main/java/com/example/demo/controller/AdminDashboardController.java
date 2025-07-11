package com.example.demo.controller;

import com.example.demo.model.io.dto.dashboard.*;
import com.example.demo.service.intface.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "12. [ADMIN] Dashboard Statistics")
public class AdminDashboardController {

    private final IDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Lấy thống kê tổng quan", description = "Cung cấp các số liệu thống kê tổng quan về toàn bộ hệ thống như tổng số người dùng, tổng số bộ thẻ, tổng số bài kiểm tra...")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = OverviewStats.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<OverviewStats> getOverviewStats() {
        OverviewStats stats = dashboardService.getOverviewStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    @Operation(summary = "Lấy thống kê người dùng", description = "Cung cấp các số liệu thống kê liên quan đến người dùng, như tổng số người dùng, người dùng mới, và người dùng hoạt động trong 7 ngày qua.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = UserStats.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<UserStats> getUserStats() {
        UserStats stats = dashboardService.getUserStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/content")
    @Operation(summary = "Lấy thống kê nội dung", description = "Cung cấp các số liệu thống kê về nội dung do người dùng tạo, như tổng số bộ thẻ/bài kiểm tra và số lượng được tạo trong 7 ngày qua.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = ContentStats.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<ContentStats> getContentStats() {
        ContentStats stats = dashboardService.getContentStats();
        return ResponseEntity.ok(stats);
    }
}