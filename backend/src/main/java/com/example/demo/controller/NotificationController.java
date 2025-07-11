package com.example.demo.controller;

import com.example.demo.model.io.response.paged.PagedNotificationResponse;
import com.example.demo.service.impl.NotificationService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "6. Notification Management")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/unread")
    @Operation(summary = "Lấy danh sách thông báo chưa đọc", description = "Lấy danh sách các thông báo chưa đọc của người dùng đang đăng nhập, có phân trang.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = PagedNotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không có thông báo chưa đọc nào")
    })
    public ResponseEntity<PagedNotificationResponse> getUnreadNotifications(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng thông báo mỗi trang") @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedNotificationResponse response = notificationService.getUnreadNotifications(pageable);
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
        // Lưu ý: API này nên lấy userId từ người dùng đang đăng nhập thay vì truyền qua parameter để bảo mật hơn.
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả thông báo", description = "Lấy toàn bộ lịch sử thông báo của người dùng đang đăng nhập, có phân trang.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = PagedNotificationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @ApiResponse(responseCode = "404", description = "Không có thông báo nào")
    })
    public ResponseEntity<PagedNotificationResponse> getAllNotifications(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng thông báo mỗi trang") @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedNotificationResponse response = notificationService.getAllNotifications(pageable);
        return ResponseEntity.ok(response);
    }
}
