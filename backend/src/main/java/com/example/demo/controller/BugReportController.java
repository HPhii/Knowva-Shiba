package com.example.demo.controller;

import com.example.demo.model.io.request.CreateBugReportRequest;
import com.example.demo.model.io.request.CreateReplyRequest;
import com.example.demo.model.io.request.UpdateStatusCommand;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.paged.PagedBugReportResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IBugReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bug-reports")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "14. Bug Report Management")
@SecurityRequirement(name = "api")
public class BugReportController {

    private final IBugReportService bugReportService;
    private final IAccountService accountService;

    @PostMapping
    @Operation(summary = "Tạo báo cáo lỗi mới", description = "Người dùng đã đăng nhập có thể gửi một báo cáo lỗi.")
    public ResponseEntity<BugReportResponse> createBugReport(@Valid @RequestBody CreateBugReportRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(bugReportService.createBugReport(request, userId));
    }

    @GetMapping("/my-reports")
    @Operation(summary = "Lấy các báo cáo lỗi của tôi", description = "Lấy danh sách các báo cáo lỗi đã được gửi bởi người dùng hiện tại.")
    public ResponseEntity<PagedBugReportResponse> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(bugReportService.getMyReports(userId, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Lấy tất cả báo cáo lỗi", description = "Admin lấy danh sách tất cả các báo cáo lỗi từ mọi người dùng.")
    public ResponseEntity<PagedBugReportResponse> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(bugReportService.getAllReports(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một báo cáo lỗi", description = "Lấy thông tin chi tiết của một báo cáo. Người dùng chỉ có thể xem báo cáo của mình, admin có thể xem mọi báo cáo.")
    public ResponseEntity<BugReportResponse> getReportById(@PathVariable Long id) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(bugReportService.getReportById(id, userId));
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Phản hồi một báo cáo lỗi", description = "Admin gửi một tin nhắn phản hồi cho một báo cáo lỗi.")
    public ResponseEntity<BugReportResponse> addReply(
            @PathVariable Long id,
            @Valid @RequestBody CreateReplyRequest request) {
        Long adminId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(bugReportService.addReply(id, request, adminId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Cập nhật trạng thái báo cáo lỗi", description = "Admin thay đổi trạng thái của một báo cáo lỗi (OPEN, IN_PROGRESS, RESOLVED, CLOSED).")
    public ResponseEntity<BugReportResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusCommand command) {
        return ResponseEntity.ok(bugReportService.updateStatus(id, command));
    }
}