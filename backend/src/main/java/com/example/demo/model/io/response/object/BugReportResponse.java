package com.example.demo.model.io.response.object;

import com.example.demo.model.enums.BugReportCategory;
import com.example.demo.model.enums.BugReportPriority;
import com.example.demo.model.enums.BugReportStatus;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BugReportResponse {
    private Long id;
    private String title;
    private String description;
    private BugReportStatus status;
    private BugReportCategory category;
    private BugReportPriority priority;
    private Long reporterId;
    private String reporterUsername;
    private Long assigneeId;
    private String assigneeUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> attachmentUrls;
    private List<ReplyResponse> replies;
}