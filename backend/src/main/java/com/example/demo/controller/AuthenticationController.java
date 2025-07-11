package com.example.demo.controller;

import com.example.demo.config.Filter;
import com.example.demo.model.entity.Account;
import com.example.demo.model.io.request.LoginRequest;
import com.example.demo.model.io.request.RegisterRequest;
import com.example.demo.model.io.response.object.AccountResponse;
import com.example.demo.service.intface.IAuthenticationService;
import com.example.demo.service.intface.ITokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
@Tag(name = "1. Authentication")
public class AuthenticationController {
    private final IAuthenticationService authenticationService;
    private final Filter filter;
    private final ITokenService tokenService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo một tài khoản mới với username, email, và password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng ký thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Thông tin không hợp lệ (ví dụ: email/username đã tồn tại)",
                    content = @Content)
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest register) {
        try {
            AccountResponse newAccount = authenticationService.register(register);
            return ResponseEntity.ok(newAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    @Operation(
            summary = "Đăng nhập tài khoản",
            description = "Nhập email và password để nhận về thông tin tài khoản và JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Sai thông tin đăng nhập (Incorrect Email or Password)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Tài khoản đã bị khóa (Your account has been banned)",
                    content = @Content
            )
    })
    public ResponseEntity<AccountResponse> loginUser(@Valid @RequestBody LoginRequest loginRequestDTO) {
        AccountResponse account = authenticationService.login(loginRequestDTO);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất tài khoản", description = "Vô hiệu hóa JWT token hiện tại của người dùng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng xuất thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ hoặc thiếu token")
    })
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = filter.getToken(request);
        if (token != null) {
            tokenService.invalidateToken(token);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Account) {
                String userId = ((Account) authentication.getPrincipal()).getId() + "";
                redisTemplate.delete("session:" + userId);
            }
            return ResponseEntity.ok("Logged out successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request or token missing.");
    }

    @PostMapping("/google")
    @Operation(summary = "Đăng nhập bằng Google", description = "Xác thực người dùng bằng Google token và trả về thông tin tài khoản cùng JWT token của hệ thống.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập Google thành công",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token Google không hợp lệ")
    })
    public ResponseEntity<AccountResponse> googleLogin(@RequestBody Map<String, String> tokenData) {
        String googleToken = tokenData.get("token");
        AccountResponse account = authenticationService.loginGoogle(googleToken);
        return ResponseEntity.ok(account);
    }
}