package com.example.demo.controller;

import com.example.demo.service.intface.ITokenService;
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
public class AdminController {
    private final RedisTemplate<String, String> redisTemplate;
    private final ITokenService tokenService;

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
}
