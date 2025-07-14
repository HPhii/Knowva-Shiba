package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.mapper.BugReportMapper;
import com.example.demo.model.entity.BugReport;
import com.example.demo.model.entity.BugReportReply;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Role;
import com.example.demo.model.io.request.CreateBugReportRequest;
import com.example.demo.model.io.request.CreateReplyRequest;
import com.example.demo.model.io.request.UpdateStatusCommand;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.paged.PagedBugReportResponse;
import com.example.demo.repository.BugReportRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IBugReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BugReportService implements IBugReportService {

    private final BugReportRepository bugReportRepository;
    private final UserRepository userRepository;
    private final BugReportMapper bugReportMapper;

    @Override
    @Transactional
    public BugReportResponse createBugReport(CreateBugReportRequest request, Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        BugReport bugReport = BugReport.builder()
                .reporter(reporter)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        return bugReportMapper.toBugReportResponse(bugReportRepository.save(bugReport));
    }

    @Override
    public PagedBugReportResponse getAllReports(Pageable pageable) {
        Page<BugReport> page = bugReportRepository.findAll(pageable);
        return new PagedBugReportResponse(
                bugReportMapper.toBugReportResponseList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }

    @Override
    public PagedBugReportResponse getMyReports(Long reporterId, Pageable pageable) {
        Page<BugReport> page = bugReportRepository.findByReporterId(reporterId, pageable);
        return new PagedBugReportResponse(
                bugReportMapper.toBugReportResponseList(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }

    @Override
    public BugReportResponse getReportById(Long id, Long userId) {
        BugReport report = bugReportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Allow access if user is the reporter or an admin
        if (!report.getReporter().getId().equals(userId) && !user.getAccount().getRole().equals(Role.ADMIN)) {
            throw new ForbiddenException("You do not have permission to view this report.");
        }

        return bugReportMapper.toBugReportResponse(report);
    }

    @Override
    @Transactional
    public BugReportResponse addReply(Long reportId, CreateReplyRequest request, Long adminId) {
        BugReport report = bugReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found"));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found"));

        BugReportReply reply = BugReportReply.builder()
                .bugReport(report)
                .user(admin)
                .message(request.getMessage())
                .build();

        report.getReplies().add(reply);
        bugReportRepository.save(report);
        return bugReportMapper.toBugReportResponse(report);
    }


    @Override
    @Transactional
    public BugReportResponse updateStatus(Long reportId, UpdateStatusCommand command) {
        BugReport report = bugReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found"));
        report.setStatus(command.getStatus());
        return bugReportMapper.toBugReportResponse(bugReportRepository.save(report));
    }
}