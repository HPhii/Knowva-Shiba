package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.FeedbackMapper;
import com.example.demo.model.entity.Feedback;
import com.example.demo.model.entity.User;
import com.example.demo.model.io.request.CreateFeedbackRequest;
import com.example.demo.model.io.response.object.FeedbackResponse;
import com.example.demo.model.io.response.paged.PagedFeedbackResponse;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final FeedbackMapper feedbackMapper;

    @Override
    public FeedbackResponse createFeedback(CreateFeedbackRequest request, Long userId) {
        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        }

        Feedback feedback = Feedback.builder()
                .user(user)
                .rating(request.getRating())
                .message(request.getMessage())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        return feedbackMapper.toFeedbackResponse(savedFeedback);
    }

    @Override
    public PagedFeedbackResponse getAllFeedback(Pageable pageable) {
        Page<Feedback> feedbackPage = feedbackRepository.findAll(pageable);
        return new PagedFeedbackResponse(
                feedbackMapper.toFeedbackResponseList(feedbackPage.getContent()),
                feedbackPage.getTotalElements(),
                feedbackPage.getTotalPages(),
                feedbackPage.getNumber()
        );
    }
}