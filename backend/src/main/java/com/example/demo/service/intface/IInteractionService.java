package com.example.demo.service.intface;

import com.example.demo.model.io.request.CreateCommentRequest;
import com.example.demo.model.io.request.CreateRatingRequest;
import com.example.demo.model.io.request.UpdateCommentRequest;
import com.example.demo.model.io.response.CommentResponse;
import com.example.demo.model.io.response.InteractionSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IInteractionService {

    // Rating operations
    void addRating(String entityType, Long entityId, CreateRatingRequest request, Long userId);
    void updateRating(String entityType, Long entityId, CreateRatingRequest request, Long userId);
    void deleteRating(String entityType, Long entityId, Long userId);

    // Comment operations
    CommentResponse addComment(String entityType, Long entityId, CreateCommentRequest request, Long userId);
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId);
    void deleteComment(Long commentId, Long userId);

    // Retrieval operations
    Page<CommentResponse> getComments(String entityType, Long entityId, Pageable pageable);
    Page<CommentResponse> getReplies(Long parentCommentId, Pageable pageable);
    InteractionSummaryResponse getInteractionSummary(String entityType, Long entityId);

    // User-specific operations
    Integer getUserRating(String entityType, Long entityId, Long userId);
}
