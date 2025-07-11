package com.example.demo.controller;

import com.example.demo.model.io.request.ResetPasswordRequest;
import com.example.demo.service.intface.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
@Tag(name = "2. Account Management")
public class AccountController {
    private final IAccountService accountService;

    @PostMapping("/send-reset-otp")
    @Operation(summary = "Gửi OTP để đặt lại mật khẩu", description = "Gửi một mã OTP đến email của người dùng để sử dụng cho việc đặt lại mật khẩu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi OTP thành công"),
            @ApiResponse(responseCode = "500", description = "Gửi OTP thất bại do lỗi máy chủ")
    })
    public ResponseEntity<?> sendResetOtp(
            @Parameter(description = "Email của người dùng để nhận OTP", required = true) @RequestParam String email) {
        try {
            accountService.sendResetOtp(email);
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP", e);
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Đặt lại mật khẩu bằng OTP", description = "Người dùng cung cấp email, OTP đã nhận và mật khẩu mới để hoàn tất việc đặt lại mật khẩu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "OTP không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "500", description = "Thất bại do lỗi máy chủ")
    })
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            accountService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reset password", e);
        }
    }

    @PostMapping("/send-verify-otp")
    @Operation(summary = "Gửi OTP để xác thực email", description = "Gửi một mã OTP đến email của người dùng để xác thực địa chỉ email là có thật.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi OTP xác thực thành công"),
            @ApiResponse(responseCode = "500", description = "Gửi OTP thất bại do lỗi máy chủ")
    })
    public ResponseEntity<?> sendVerifyOtp(
            @Parameter(description = "Email cần xác thực", required = true) @RequestParam String email) {
        try {
            accountService.sendVerifyOtp(email);
            return ResponseEntity.ok("Verification OTP sent successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification OTP", e);
        }
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Xác thực email bằng OTP", description = "Hoàn tất việc xác thực email bằng cách cung cấp email và mã OTP tương ứng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác thực email thành công"),
            @ApiResponse(responseCode = "400", description = "OTP không hợp lệ hoặc đã hết hạn"),
            @ApiResponse(responseCode = "500", description = "Thất bại do lỗi máy chủ")
    })
    public ResponseEntity<?> verifyEmail(
            @Parameter(description = "Email cần xác thực", required = true) @RequestParam String email,
            @Parameter(description = "Mã OTP đã nhận", required = true) @RequestParam String otp) {
        try {
            accountService.verifyEmail(email, otp);
            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify email", e);
        }
    }
}