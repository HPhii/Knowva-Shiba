package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Status;
import com.example.demo.model.io.dto.UpdateUserProfileDTO;
import com.example.demo.model.io.response.object.UserProfileResponse;
import com.example.demo.model.io.response.paged.PagedUsersResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IPaymentService;
import com.example.demo.service.intface.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "3. User Management")
public class UserController {
    private final IUserService userService;
    private final IAccountService accountService;
    private final IPaymentService paymentService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Lấy danh sách người dùng", description = "Chỉ Admin có quyền. Lấy danh sách người dùng đã được phân trang và lọc theo trạng thái.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = PagedUsersResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<PagedUsersResponse> getAllUsers(
            @Parameter(description = "Trạng thái tài khoản (ACTIVE, INACTIVE, BANNED)", required = true) @RequestParam Status status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử mỗi trang") @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedUsersResponse response = userService.getAllUsers(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin hồ sơ người dùng bằng ID", description = "Lấy thông tin chi tiết của một người dùng dựa trên ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<?> getUserProfile(
            @Parameter(description = "ID của người dùng cần xem", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin hồ sơ của tôi", description = "Lấy thông tin chi tiết của người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    public ResponseEntity<?> getMyProfile() {
        User user = accountService.getCurrentAccount().getUser();
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Xóa (Vô hiệu hóa) người dùng", description = "Chỉ Admin có quyền. Thay đổi trạng thái người dùng thành INACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vô hiệu hóa thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID của người dùng cần xóa", required = true) @PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Cập nhật thông tin hồ sơ", description = "Cập nhật thông tin cho người dùng. Admin có thể cập nhật cho bất kỳ ai, người dùng thường chỉ có thể tự cập nhật cho mình.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = UpdateUserProfileDTO.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<?> updateUserProfile(
            @Parameter(description = "ID của người dùng cần cập nhật", required = true) @PathVariable Long id,
            @RequestBody @Valid UpdateUserProfileDTO updateUserProfileDTO) {
        return ResponseEntity.ok(userService.updateUserProfile(id, updateUserProfileDTO));
    }

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Lấy lịch sử giao dịch", description = "Lấy danh sách các giao dịch của người dùng. Admin có thể xem của bất kỳ ai, người dùng thường chỉ có thể tự xem của mình.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng")
    })
    public ResponseEntity<?> getUserTransactions(
            @Parameter(description = "ID của người dùng cần xem lịch sử giao dịch", required = true) @PathVariable Long id){
        return ResponseEntity.ok(paymentService.getTransactionsByUserId(id));
    }
}