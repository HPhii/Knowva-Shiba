package com.example.demo.model.io.response.paged;

import com.example.demo.model.io.response.object.FeedbackResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagedFeedbackResponse {
    private List<FeedbackResponse> feedbacks;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}