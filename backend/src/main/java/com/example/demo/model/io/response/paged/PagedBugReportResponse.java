package com.example.demo.model.io.response.paged;

import com.example.demo.model.io.response.object.BugReportResponse;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedBugReportResponse {
    private List<BugReportResponse> reports;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}