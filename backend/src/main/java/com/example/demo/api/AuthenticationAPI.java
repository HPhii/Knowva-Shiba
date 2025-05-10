package com.example.demo.api;

import com.example.demo.config.Filter;
import com.example.demo.model.io.request.RegisterRequest;
import com.example.demo.model.io.response.object.AccountResponse;
import com.example.demo.model.io.request.LoginRequest;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IAuthenticationService;
import com.example.demo.service.intface.ITokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@RequiredArgsConstructor
public class AuthenticationAPI {
    private final IAccountService accountService;
    private final IAuthenticationService authenticationService;
    private final Filter filter;
    private final ITokenService tokenService;
    private final AccountRepository accountRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest register) {
        try {
            AccountResponse newAccount = authenticationService.register(register);
            return ResponseEntity.ok(newAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AccountResponse> loginUser(@Valid @RequestBody LoginRequest loginRequestDTO) {
        AccountResponse account = authenticationService.login(loginRequestDTO);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String token = filter.getToken(request);
        if (token != null) {
            tokenService.invalidateToken(token); // Add logic to blacklist the token.
            return ResponseEntity.ok("Logged out successfully.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request or token missing.");
    }

    @PostMapping("/google")
    public ResponseEntity<AccountResponse> googleLogin(@RequestBody Map<String, String> tokenData) {
        String googleToken = tokenData.get("token");
        AccountResponse account = authenticationService.loginGoogle(googleToken);
        return ResponseEntity.ok(account);
    }
}
