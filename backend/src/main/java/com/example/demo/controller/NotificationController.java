package com.example.demo.controller;

import com.example.demo.model.enums.NotificationType;
import com.example.demo.model.io.response.paged.PagedNotificationResponse;
import com.example.demo.service.intface.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "6. Notification Management")
public class NotificationController {
    private final INotificationService notificationService;

    @GetMapping
    @Operation(summary = "Lấy tất cả thông báo với bộ lọc", description = "Lấy toàn bộ lịch sử thông báo của người dùng đang đăng nhập, có phân trang và hỗ trợ lọc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = PagedNotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không có thông báo nào khớp với tiêu chí")
    })
    public ResponseEntity<PagedNotificationResponse> getAllNotifications(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng thông báo mỗi trang") @RequestParam(defaultValue = "8") int size,
            @Parameter(description = "Sắp xếp theo trường (vd: timestamp)") @RequestParam(defaultValue = "timestamp") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Lọc theo trạng thái đã đọc (true/false)") @RequestParam(required = false) Boolean isRead,
            @Parameter(description = "Lọc theo loại thông báo") @RequestParam(required = false) NotificationType type,
            @Parameter(description = "Lọc từ ngày (format: yyyy-MM-dd'T'HH:mm:ss)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Lọc đến ngày (format: yyyy-MM-dd'T'HH:mm:ss)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PagedNotificationResponse response = notificationService.getNotifications(isRead, type, startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Đánh dấu một thông báo là đã đọc", description = "Đánh dấu một thông báo cụ thể là đã đọc dựa trên ID của nó.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "ID của thông báo cần đánh dấu đã đọc", required = true) @PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    @Operation(summary = "Đánh dấu tất cả thông báo là đã đọc", description = "Đánh dấu tất cả thông báo chưa đọc của một người dùng là đã đọc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh dấu tất cả thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng hoặc không có thông báo chưa đọc")
    })
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "ID của người dùng cần đánh dấu tất cả thông báo là đã đọc", required = true) @RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}