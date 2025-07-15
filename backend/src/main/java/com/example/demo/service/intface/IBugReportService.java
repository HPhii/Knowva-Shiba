package com.example.demo.service.intface;

import com.example.demo.model.enums.BugReportCategory;
import com.example.demo.model.enums.BugReportPriority;
import com.example.demo.model.enums.BugReportStatus;
import com.example.demo.model.io.request.CreateBugReportRequest;
import com.example.demo.model.io.request.CreateReplyRequest;
import com.example.demo.model.io.request.UpdateStatusCommand;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.paged.PagedBugReportResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBugReportService {
    BugReportResponse createBugReport(CreateBugReportRequest request, Long reporterId, List<MultipartFile> attachments);
    PagedBugReportResponse getAllReports(BugReportStatus status, BugReportCategory category, BugReportPriority priority, Pageable pageable);
    PagedBugReportResponse getMyReports(Long reporterId, BugReportStatus status, BugReportCategory category, BugReportPriority priority, Pageable pageable);
    BugReportResponse getReportById(Long id, Long userId);
    BugReportResponse addReply(Long reportId, CreateReplyRequest request, Long adminId);
    BugReportResponse updateStatus(Long reportId, UpdateStatusCommand command);
    BugReportResponse addReporterReply(Long reportId, CreateReplyRequest request, Long reporterId);
    BugReportResponse assignReport( Long reportId, Long adminId);
}