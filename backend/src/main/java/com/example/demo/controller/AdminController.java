package com.example.demo.controller;

import com.example.demo.model.entity.PaymentTransaction;
import com.example.demo.model.enums.NotificationType;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.INotificationService;
import com.example.demo.service.intface.IPaymentService;
import com.example.demo.service.intface.ITokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "11. [ADMIN] Admin Management")
public class AdminController {
    private final RedisTemplate<String, String> redisTemplate;
    private final ITokenService tokenService;
    private final INotificationService notificationService;
    private final IAccountService accountService;
    private final IPaymentService paymentService;

    @PostMapping("/force-logout/{userId}")
    @Operation(summary = "Buộc người dùng đăng xuất", description = "Buộc một người dùng cụ thể đăng xuất khỏi hệ thống bằng cách vô hiệu hóa token của họ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không có phiên hoạt động nào cho người dùng này")
    })
    public ResponseEntity<String> forceLogout(
            @Parameter(description = "ID của người dùng cần buộc đăng xuất", required = true) @PathVariable Long userId) {
        String sessionKey = "session:" + userId;
        String token = redisTemplate.opsForValue().get(sessionKey);
        if (token != null) {
            tokenService.invalidateToken(token);
            redisTemplate.delete(sessionKey);
            return ResponseEntity.ok("User logged out successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active session for this user.");
    }

    @PostMapping("/send-system-notification")
    @Operation(summary = "Gửi thông báo toàn hệ thống", description = "Gửi một thông báo đến tất cả người dùng trong hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi thông báo thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<String> sendSystemNotification(
            @Parameter(description = "Loại thông báo (SYSTEM_ALERT, REMINDER, etc.)", required = true) @RequestParam NotificationType type,
            @Parameter(description = "Nội dung thông báo", required = true) @RequestParam String message,
            @Parameter(description = "ID của set (quiz hoặc flashcard) liên quan (tùy chọn)") @RequestParam(required = false) Long setId) {
        notificationService.createSystemNotification(type, message, setId);
        return ResponseEntity.ok("System notification sent successfully.");
    }

    @PatchMapping("/ban-user/{id}")
    @Operation(summary = "Cấm (ban) tài khoản người dùng", description = "Thay đổi trạng thái tài khoản của người dùng thành BANNED.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cấm người dùng thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<String> banUser(
            @Parameter(description = "ID của người dùng cần cấm", required = true) @PathVariable Long id) {
        accountService.banUser(id);
        return ResponseEntity.ok("User banned successfully.");
    }

    @PatchMapping("/upgrade-to-premium/{accountId}")
    @Operation(summary = "Nâng cấp tài khoản lên VIP", description = "Nâng cấp vai trò của một tài khoản thành VIP mà không cần qua cổng thanh toán.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nâng cấp thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy tài khoản")
    })
    public ResponseEntity<String> upgradeToPremium(
            @Parameter(description = "ID của tài khoản cần nâng cấp", required = true) @PathVariable Long accountId) {
        accountService.upgradeToPremium(accountId);
        return ResponseEntity.ok("User upgraded to premium successfully.");
    }

    @GetMapping("/transactions")
    @Operation(summary = "Lấy tất cả lịch sử giao dịch", description = "Lấy danh sách toàn bộ các giao dịch đã được thực hiện trên hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<List<PaymentTransaction>> getAllTransactions() {
        return ResponseEntity.ok(paymentService.getAllTransactions());
    }
}