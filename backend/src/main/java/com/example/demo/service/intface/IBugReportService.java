package com.example.demo.service.intface;

import com.example.demo.model.io.request.CreateBugReportRequest;
import com.example.demo.model.io.request.CreateReplyRequest;
import com.example.demo.model.io.request.UpdateStatusCommand;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.paged.PagedBugReportResponse;
import org.springframework.data.domain.Pageable;

public interface IBugReportService {
    BugReportResponse createBugReport(CreateBugReportRequest request, Long reporterId);
    PagedBugReportResponse getAllReports(Pageable pageable);
    PagedBugReportResponse getMyReports(Long reporterId, Pageable pageable);
    BugReportResponse getReportById(Long id, Long userId);
    BugReportResponse addReply(Long reportId, CreateReplyRequest request, Long adminId);
    BugReportResponse updateStatus(Long reportId, UpdateStatusCommand command);
}