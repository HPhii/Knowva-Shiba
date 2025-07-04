package com.example.demo.controller;

import com.example.demo.model.enums.NotificationType;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.INotificationService;
import com.example.demo.service.intface.ITokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class AdminController {
    private final RedisTemplate<String, String> redisTemplate;
    private final ITokenService tokenService;
    private final INotificationService notificationService;
    private final IAccountService accountService;

    @PostMapping("/force-logout/{userId}")
    public ResponseEntity<String> forceLogout(@PathVariable Long userId) {
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
    public ResponseEntity<String> sendSystemNotification(
            @RequestParam NotificationType type,
            @RequestParam String message,
            @RequestParam(required = false) Long setId) {
        notificationService.createSystemNotification(type, message, setId);
        return ResponseEntity.ok("System notification sent successfully.");
    }

    @PatchMapping("/ban-user/{id}")
    public ResponseEntity<String> banUser(@PathVariable Long id) {
        accountService.banUser(id);
        return ResponseEntity.ok("User banned successfully.");
    }

    @PatchMapping("/upgrade-to-premium/{accountId}")
    public ResponseEntity<String> upgradeToPremium(@PathVariable Long accountId) {
        accountService.upgradeToPremium(accountId);
        return ResponseEntity.ok("User upgraded to premium successfully.");
    }
}