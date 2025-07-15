package com.example.demo.model.io.request;

import com.example.demo.model.enums.BugReportCategory;
import com.example.demo.model.enums.BugReportPriority;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBugReportRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    private BugReportCategory category;

    private BugReportPriority priority;

    // Note: Files will be handled as MultipartFile[] in the controller, not in this DTO.
}