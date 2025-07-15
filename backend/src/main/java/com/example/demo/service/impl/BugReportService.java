package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.mapper.BugReportMapper;
import com.example.demo.model.entity.BugReport;
import com.example.demo.model.entity.BugReportReply;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.*;
import com.example.demo.model.io.request.CreateBugReportRequest;
import com.example.demo.model.io.request.CreateReplyRequest;
import com.example.demo.model.io.request.UpdateStatusCommand;
import com.example.demo.model.io.response.object.BugReportResponse;
import com.example.demo.model.io.response.paged.PagedBugReportResponse;
import com.example.demo.repository.BugReportRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IBugReportService;
import com.example.demo.service.intface.ICloudinaryService;
import com.example.demo.service.intface.INotificationService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.specification.BugReportSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BugReportService implements IBugReportService {

    private final BugReportRepository bugReportRepository;
    private final UserRepository userRepository;
    private final BugReportMapper bugReportMapper;
    private final ICloudinaryService cloudinaryService;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public BugReportResponse createBugReport(CreateBugReportRequest request, Long reporterId, List<MultipartFile> attachments) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<String> attachmentUrls = new ArrayList<>();
        if (attachments != null && !attachments.isEmpty()) {
            attachmentUrls = attachments.stream()
                    .map(cloudinaryService::uploadImage)
                    .collect(Collectors.toList());
        }

        BugReport bugReport = BugReport.builder()
                .reporter(reporter)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .priority(request.getPriority())
                .attachmentUrls(attachmentUrls)
                .build();

        BugReport savedReport = bugReportRepository.save(bugReport);

        String notiMessageForAdmins = "New bug report created by " + reporter.getAccount().getUsername() + ": '" + truncate(savedReport.getTitle()) + "'";
        notificationService.createNotificationForAdmins(NotificationType.BUG_REPORT, notiMessageForAdmins, savedReport.getId());

        return bugReportMapper.toBugReportResponse(savedReport);
    }

    @Override
    public PagedBugReportResponse getAllReports(BugReportStatus status, BugReportCategory category, BugReportPriority priority, Pageable pageable) {
        Specification<BugReport> spec = Specification.where(null);
        return getPagedBugReportResponse(status, category, priority, pageable, spec);
    }

    @Override
    public PagedBugReportResponse getMyReports(Long reporterId, BugReportStatus status, BugReportCategory category, BugReportPriority priority, Pageable pageable) {
        Specification<BugReport> spec = BugReportSpecification.withReporterId(reporterId);
        return getPagedBugReportResponse(status, category, priority, pageable, spec);
    }

    private PagedBugReportResponse getPagedBugReportResponse(BugReportStatus status, BugReportCategory category, BugReportPriority priority, Pageable pageable, Specification<BugReport> spec) {
        if (status != null) {
            spec = spec.and(BugReportSpecification.withStatus(status));
        }
        if (category != null) {
            spec = spec.and(BugReportSpecification.withCategory(category));
        }
        if (priority != null) {
            spec = spec.and(BugReportSpecification.withPriority(priority));
        }

        Page<BugReport> page = bugReportRepository.findAll(spec, pageable);
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

        BugReportReply reply = createReply(report, admin, request);

        report.getReplies().add(reply);
        report.setStatus(BugReportStatus.IN_PROGRESS);
        BugReport savedReport = bugReportRepository.save(report);

        Hibernate.initialize(savedReport.getReplies());
        Hibernate.initialize(savedReport.getReporter().getAccount());

        // Notify user about the reply
        String notiMessage = "Admin has replied to your bug report: '" + truncate(report.getTitle()) + "'";
        notificationService.createNotification(report.getReporter().getId(), NotificationType.BUG_REPORT, notiMessage, reportId);

        return bugReportMapper.toBugReportResponse(savedReport);
    }

    @Override
    @Transactional
    public BugReportResponse addReporterReply(Long reportId, CreateReplyRequest request, Long reporterId) {
        BugReport report = bugReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found"));

        if (!report.getReporter().getId().equals(reporterId)) {
            throw new ForbiddenException("You do not have permission to reply to this report.");
        }

        User reporter = report.getReporter();
        BugReportReply reply = createReply(report, reporter, request);

        report.getReplies().add(reply);

        if (report.getStatus() == BugReportStatus.RESOLVED || report.getStatus() == BugReportStatus.CLOSED) {
            report.setStatus(BugReportStatus.OPEN);
        }

        BugReport savedReport = bugReportRepository.save(report);
        Hibernate.initialize(savedReport.getReplies());
        Hibernate.initialize(savedReport.getReporter().getAccount());

        String notiMessage = "User '" + reporter.getAccount().getUsername() + "' has replied to bug report: '" + truncate(report.getTitle()) + "'";
        notificationService.createNotificationForAdmins(NotificationType.BUG_REPORT, notiMessage, report.getId());

        return bugReportMapper.toBugReportResponse(savedReport);
    }

    @Override
    @Transactional
    public BugReportResponse updateStatus(Long reportId, UpdateStatusCommand command) {
        BugReport report = bugReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found"));
        report.setStatus(command.getStatus());
        BugReport savedReport = bugReportRepository.save(report);

        // Notify user about the status change
        String notiMessage = "The status of your bug report '" + truncate(report.getTitle()) + "' has been updated to " + command.getStatus().name();
        notificationService.createNotification(report.getReporter().getId(), NotificationType.BUG_REPORT, notiMessage, reportId);

        return bugReportMapper.toBugReportResponse(savedReport);
    }

    @Override
    @Transactional
    public BugReportResponse assignReport(Long reportId, Long adminId) {
        BugReport report = bugReportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Bug report not found"));
        User adminToAssign = userRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found"));

        if (!adminToAssign.getAccount().getRole().equals(Role.ADMIN)) {
            throw new IllegalArgumentException("User is not an admin.");
        }

        report.setAssignee(adminToAssign);
        BugReport savedReport = bugReportRepository.save(report);

        String notiMessageForAdmin = "You have been assigned to handle the bug report: '" + truncate(report.getTitle()) + "'";
        notificationService.createNotification(adminId, NotificationType.BUG_REPORT, notiMessageForAdmin, reportId);

        String notiMessageForReporter = "Your bug report '" + truncate(report.getTitle()) + "' has been assigned to an admin for handling.";
        notificationService.createNotification(report.getReporter().getId(), NotificationType.BUG_REPORT, notiMessageForReporter, reportId);

        return bugReportMapper.toBugReportResponse(savedReport);
    }

    private BugReportReply createReply(BugReport report, User user, CreateReplyRequest request) {
        BugReportReply parentReply = null;
        if (request.getParentId() != null) {
            parentReply = report.getReplies().stream()
                    .filter(r -> r.getId().equals(request.getParentId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Parent reply not found"));
        }

        return BugReportReply.builder()
                .bugReport(report)
                .user(user)
                .message(request.getMessage())
                .parent(parentReply)
                .build();
    }

    private String truncate(String text) {
        if (text == null || text.length() <= 30) {
            return text;
        }
        return text.substring(0, 30) + "...";
    }
}