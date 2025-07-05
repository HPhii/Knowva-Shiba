package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Status;
import com.example.demo.model.io.dto.UpdateUserProfileDTO;
import com.example.demo.model.io.response.paged.PagedUsersResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IPaymentService;
import com.example.demo.service.intface.IUserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class UserController {
    private final IUserService userService;
    private final IAccountService accountService;
    private final IPaymentService paymentService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedUsersResponse> getAllUsers(@RequestParam Status status,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedUsersResponse response = userService.getAllUsers(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        User user = accountService.getCurrentAccount().getUser();
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long id, @RequestBody @Valid UpdateUserProfileDTO updateUserProfileDTO) {
        return ResponseEntity.ok(userService.updateUserProfile(id, updateUserProfileDTO));
    }

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long id){
        return ResponseEntity.ok(paymentService.getTransactionsByUserId(id));
    }
}