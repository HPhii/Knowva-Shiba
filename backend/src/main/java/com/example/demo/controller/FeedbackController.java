package com.example.demo.controller;

import com.example.demo.model.io.request.CreateFeedbackRequest;
import com.example.demo.model.io.response.object.FeedbackResponse;
import com.example.demo.model.io.response.paged.PagedFeedbackResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/feedback")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "13. Feedback Management")
public class FeedbackController {

    private final IFeedbackService feedbackService;
    private final IAccountService accountService;

    @PostMapping
    @Operation(summary = "Gửi feedback", description = "Cho phép người dùng (cả đã đăng nhập và ẩn danh) gửi feedback về ứng dụng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi feedback thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    public ResponseEntity<FeedbackResponse> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        FeedbackResponse response = feedbackService.createFeedback(request, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "api")
    @Operation(summary = "[ADMIN] Lấy tất cả feedback", description = "Chỉ Admin có quyền. Lấy danh sách tất cả feedback từ người dùng, có phân trang.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<PagedFeedbackResponse> getAllFeedback(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedFeedbackResponse response = feedbackService.getAllFeedback(pageable);
        return ResponseEntity.ok(response);
    }
}