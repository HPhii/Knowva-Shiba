package com.example.demo.model.io.request;

import com.example.demo.model.enums.BugReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusCommand {
    @NotNull(message = "Status cannot be null")
    private BugReportStatus status;
}