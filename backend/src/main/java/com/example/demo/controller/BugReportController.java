package com.example.demo.controller;

import com.example.demo.model.enums.BugReportCategory;
import com.example.demo.model.enums.BugReportPriority;
import com.example.demo.model.enums.BugReportStatus;
import com.example.demo.model.io.request.CreateBugReportRequest;
import com.example.demo.model.io.request.CreateReplyRequest;
import com.example.demo.model.io.request.UpdateStatusCommand;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.paged.PagedBugReportResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IBugReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bug-reports")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "14. Bug Report Management")
@SecurityRequirement(name = "api")
public class BugReportController {

    private final IBugReportService bugReportService;
    private final IAccountService accountService;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Tạo báo cáo lỗi mới (POSTMAN)", description = "Người dùng đã đăng nhập có thể gửi một báo cáo lỗi, đính kèm file nếu cần.")
    public ResponseEntity<BugReportResponse> createBugReport(
            @Valid @RequestPart("report") CreateBugReportRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(bugReportService.createBugReport(request, userId, attachments));
    }

    @GetMapping("/my-reports")
    @Operation(summary = "Lấy các báo cáo lỗi của tôi", description = "Lấy danh sách các báo cáo lỗi đã được gửi bởi người dùng hiện tại, có hỗ trợ lọc và sắp xếp.")
    public ResponseEntity<PagedBugReportResponse> getMyReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) BugReportStatus status,
            @Parameter(description = "Lọc theo danh mục") @RequestParam(required = false) BugReportCategory category,
            @Parameter(description = "Lọc theo độ ưu tiên") @RequestParam(required = false) BugReportPriority priority,
            @Parameter(description = "Sắp xếp theo trường (vd: createdAt, priority, status)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(bugReportService.getMyReports(userId, status, category, priority, pageable));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Lấy tất cả báo cáo lỗi", description = "Admin lấy danh sách tất cả các báo cáo lỗi từ mọi người dùng, có hỗ trợ lọc và sắp xếp.")
    public ResponseEntity<PagedBugReportResponse> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) BugReportStatus status,
            @Parameter(description = "Lọc theo danh mục") @RequestParam(required = false) BugReportCategory category,
            @Parameter(description = "Lọc theo độ ưu tiên") @RequestParam(required = false) BugReportPriority priority,
            @Parameter(description = "Sắp xếp theo trường (vd: createdAt, priority, status)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(bugReportService.getAllReports(status, category, priority, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một báo cáo lỗi", description = "Lấy thông tin chi tiết của một báo cáo. Người dùng chỉ có thể xem báo cáo của mình, admin có thể xem mọi báo cáo.")
    public ResponseEntity<BugReportResponse> getReportById(@PathVariable Long id) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(bugReportService.getReportById(id, userId));
    }

    @PostMapping("/{id}/user-reply")
    @Operation(summary = "Người dùng phản hồi lại báo cáo lỗi", description = "Cho phép người dùng đã tạo báo cáo lỗi có thể gửi phản hồi tiếp theo. Tự động mở lại ticket nếu đã đóng.")
    public ResponseEntity<BugReportResponse> addUserReply(
            @PathVariable Long id,
            @Valid @RequestBody CreateReplyRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(bugReportService.addReporterReply(id, request, userId));
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

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Gán báo cáo cho một Admin", description = "Admin gán một báo cáo lỗi cho một admin khác hoặc cho chính mình.")
    public ResponseEntity<BugReportResponse> assignReport(
            @PathVariable Long id,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(bugReportService.assignReport(id, adminId));
    }
}