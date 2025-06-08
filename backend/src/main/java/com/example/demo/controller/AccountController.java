package com.example.demo.controller;

import com.example.demo.model.io.request.ResetPasswordRequest;
import com.example.demo.service.intface.IAccountService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class AccountController {
    private final IAccountService accountService;

    @PostMapping("/send-reset-otp")
    public ResponseEntity<?> sendResetOtp(@RequestParam String email) {
        try {
            accountService.sendResetOtp(email);
            return ResponseEntity.ok("OTP sent successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP", e);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            accountService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to reset password", e);
        }
    }

    @PostMapping("/send-verify-otp")
    public ResponseEntity<?> sendVerifyOtp(@RequestParam String email) {
        try {
            accountService.sendVerifyOtp(email);
            return ResponseEntity.ok("Verification OTP sent successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification OTP", e);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        try {
            accountService.verifyEmail(email, otp);
            return ResponseEntity.ok("Email verified successfully");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to verify email", e);
        }
    }
}
