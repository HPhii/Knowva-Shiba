package com.example.demo.service.intface;

import com.example.demo.model.io.request.CreateFeedbackRequest;
import com.example.demo.model.io.response.object.FeedbackResponse;
import com.example.demo.model.io.response.paged.PagedFeedbackResponse;
import org.springframework.data.domain.Pageable;

public interface IFeedbackService {
    FeedbackResponse createFeedback(CreateFeedbackRequest request);
    PagedFeedbackResponse getAllFeedback(Pageable pageable);
}