package com.example.demo.model.entity;

import com.example.demo.model.enums.BugReportCategory;
import com.example.demo.model.enums.BugReportPriority;
import com.example.demo.model.enums.BugReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bug_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugReportStatus status;

    @Enumerated(EnumType.STRING)
    private BugReportCategory category;

    @Enumerated(EnumType.STRING)
    private BugReportPriority priority;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "bug_report_attachments", joinColumns = @JoinColumn(name = "bug_report_id"))
    @Column(name = "attachment_url", length = 512)
    private List<String> attachmentUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "bugReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<BugReportReply> replies = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = BugReportStatus.OPEN;
        if (priority == null) {
            priority = BugReportPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}