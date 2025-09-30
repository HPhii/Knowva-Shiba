package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.ForbiddenException;
import com.example.demo.model.entity.*;
import com.example.demo.model.enums.ActivityType;
import com.example.demo.model.io.request.CreateCommentRequest;
import com.example.demo.model.io.request.CreateRatingRequest;
import com.example.demo.model.io.request.UpdateCommentRequest;
import com.example.demo.model.io.response.CommentResponse;
import com.example.demo.model.io.response.InteractionSummaryResponse;
import com.example.demo.repository.*;
import com.example.demo.service.intface.IInteractionService;
import com.example.demo.service.intface.IActivityLogService;
import com.example.demo.service.strategy.EntityInteractionStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InteractionService implements IInteractionService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final EntityInteractionStrategyFactory strategyFactory;
    private final IActivityLogService activityLogService;

    @Override
    public void addRating(String entityType, Long entityId, CreateRatingRequest request, Long userId) {
        User user = getUserById(userId);
        var strategy = strategyFactory.getStrategy(entityType);

        // Check if user already rated this entity
        Optional<Rating> existingRating = findExistingRating(entityType, entityId, user);
        if (existingRating.isPresent()) {
            throw new IllegalArgumentException("User has already rated this " + entityType);
        }

        // Verify entity exists
        strategy.findEntityById(entityId);

        Rating rating = Rating.builder()
                .user(user)
                .ratingValue(request.getRatingValue())
                .build();

        strategy.setEntityForRating(rating, entityId);
        Rating savedRating = ratingRepository.save(rating);

        // === GHI LOG HOẠT ĐỘNG ===
        String description = String.valueOf(request.getRatingValue()); // Giữ rating value cho frontend
        activityLogService.logActivity(user, ActivityType.RATE_CONTENT, description, entityId);
        // =========================
    }

    @Override
    public void updateRating(String entityType, Long entityId, CreateRatingRequest request, Long userId) {
        User user = getUserById(userId);

        Optional<Rating> existingRating = findExistingRating(entityType, entityId, user);
        if (existingRating.isEmpty()) {
            throw new EntityNotFoundException("Rating not found for this user and " + entityType);
        }

        Rating rating = existingRating.get();
        rating.setRatingValue(request.getRatingValue());
        ratingRepository.save(rating);
    }

    @Override
    public void deleteRating(String entityType, Long entityId, Long userId) {
        User user = getUserById(userId);

        Optional<Rating> existingRating = findExistingRating(entityType, entityId, user);
        if (existingRating.isEmpty()) {
            throw new EntityNotFoundException("Rating not found for this user and " + entityType);
        }

        ratingRepository.delete(existingRating.get());
    }

    @Override
    public CommentResponse addComment(String entityType, Long entityId, CreateCommentRequest request, Long userId) {
        User user = getUserById(userId);
        var strategy = strategyFactory.getStrategy(entityType);

        // Verify entity exists
        strategy.findEntityById(entityId);

        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .imageUrl(request.getImageUrl()) // Thêm dòng này
                .build();

        // Handle parent comment if it's a reply
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        strategy.setEntityForComment(comment, entityId);
        Comment savedComment = commentRepository.save(comment);

        // === GHI LOG HOẠT ĐỘNG ===
        // Chỉ log cho comment chính, không log cho reply để tránh spam
        if (request.getParentId() == null) {
            activityLogService.logActivity(user, ActivityType.COMMENT_ON_CONTENT, "", entityId);
        }
        // =========================

        return mapToCommentResponse(savedComment);
    }

    @Override
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        if (comment.getIsDeleted()) {
            throw new IllegalArgumentException("Cannot edit deleted comment");
        }

        comment.setContent(request.getContent());
        comment.setImageUrl(request.getImageUrl()); // Thêm dòng này
        Comment savedComment = commentRepository.save(comment);

        return mapToCommentResponse(savedComment);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        // Soft delete
        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(String entityType, Long entityId, Pageable pageable) {
        Page<Comment> comments = switch (entityType.toLowerCase()) {
            case "quizset" -> commentRepository.findByQuizSetIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(entityId, pageable);
            case "flashcardset" -> commentRepository.findByFlashcardSetIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(entityId, pageable);
            case "blogpost" -> commentRepository.findByBlogPostIdAndParentCommentIsNullAndIsDeletedFalseOrderByCreatedAtDesc(entityId, pageable);
            default -> throw new IllegalArgumentException("Invalid entity type: " + entityType);
        };

        return comments.map(this::mapToCommentResponseWithReplies);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getReplies(Long parentCommentId, Pageable pageable) {
        // For simplicity, convert List to Page (in real scenario, you might want to implement pagination at DB level)
        List<Comment> replies = commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(parentCommentId);
        return replies.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList())
                .stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, replies.size())
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public InteractionSummaryResponse getInteractionSummary(String entityType, Long entityId) {
        Double averageRating = getAverageRating(entityType, entityId);
        Long totalRatings = getTotalRatings(entityType, entityId);
        Long totalComments = getTotalComments(entityType, entityId);

        InteractionSummaryResponse.RatingDistribution distribution = buildRatingDistribution(entityType, entityId);

        return InteractionSummaryResponse.builder()
                .averageRating(averageRating)
                .totalRatings(totalRatings)
                .totalComments(totalComments)
                .ratingDistribution(distribution) // Thêm dòng này
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUserRating(String entityType, Long entityId, Long userId) {
        User user = getUserById(userId);
        Optional<Rating> rating = findExistingRating(entityType, entityId, user);
        return rating.map(Rating::getRatingValue).orElse(null);
    }

    // Helper methods
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private Optional<Rating> findExistingRating(String entityType, Long entityId, User user) {
        return switch (entityType.toLowerCase()) {
            case "quizset" -> ratingRepository.findByUserAndQuizSetId(user, entityId);
            case "flashcardset" -> ratingRepository.findByUserAndFlashcardSetId(user, entityId);
            case "blogpost" -> ratingRepository.findByUserAndBlogPostId(user, entityId);
            default -> throw new IllegalArgumentException("Invalid entity type: " + entityType);
        };
    }

    private Double getAverageRating(String entityType, Long entityId) {
        return switch (entityType.toLowerCase()) {
            case "quizset" -> ratingRepository.getAverageRatingForQuizSet(entityId);
            case "flashcardset" -> ratingRepository.getAverageRatingForFlashcardSet(entityId);
            case "blogpost" -> ratingRepository.getAverageRatingForBlogPost(entityId);
            default -> throw new IllegalArgumentException("Invalid entity type: " + entityType);
        };
    }

    private Long getTotalRatings(String entityType, Long entityId) {
        return switch (entityType.toLowerCase()) {
            case "quizset" -> ratingRepository.countByQuizSetId(entityId);
            case "flashcardset" -> ratingRepository.countByFlashcardSetId(entityId);
            case "blogpost" -> ratingRepository.countByBlogPostId(entityId);
            default -> throw new IllegalArgumentException("Invalid entity type: " + entityType);
        };
    }

    private Long getTotalComments(String entityType, Long entityId) {
        return switch (entityType.toLowerCase()) {
            case "quizset" -> commentRepository.countByQuizSetIdAndIsDeletedFalse(entityId);
            case "flashcardset" -> commentRepository.countByFlashcardSetIdAndIsDeletedFalse(entityId);
            case "blogpost" -> commentRepository.countByBlogPostIdAndIsDeletedFalse(entityId);
            default -> throw new IllegalArgumentException("Invalid entity type: " + entityType);
        };
    }

    private CommentResponse mapToCommentResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl()) // Thêm dòng này
                .userName(comment.getUser().getFullName())
                .userAvatarUrl(comment.getUser().getAvatarUrl())
                .userId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .build();
    }

    private CommentResponse mapToCommentResponseWithReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAtAsc(comment.getId());
        List<CommentResponse> replyResponses = replies.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .imageUrl(comment.getImageUrl()) // Thêm dòng này
                .userName(comment.getUser().getFullName())
                .userAvatarUrl(comment.getUser().getAvatarUrl())
                .userId(comment.getUser().getId())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .replies(replyResponses)
                .replyCount(replyResponses.size())
                .build();
    }

    private InteractionSummaryResponse.RatingDistribution buildRatingDistribution(String entityType, Long entityId) {
        List<RatingRepository.RatingCount> counts = switch (entityType.toLowerCase()) {
            case "quizset" -> ratingRepository.getRatingDistributionForQuizSet(entityId);
            case "flashcardset" -> ratingRepository.getRatingDistributionForFlashcardSet(entityId);
            case "blogpost" -> ratingRepository.getRatingDistributionForBlogPost(entityId);
            default -> throw new IllegalArgumentException("Invalid entity type: " + entityType);
        };

        Map<Integer, Long> countsMap = counts.stream()
                .collect(Collectors.toMap(RatingRepository.RatingCount::getRatingValue, RatingRepository.RatingCount::getCount));

        return InteractionSummaryResponse.RatingDistribution.builder()
                .oneStar(countsMap.getOrDefault(1, 0L))
                .twoStar(countsMap.getOrDefault(2, 0L))
                .threeStar(countsMap.getOrDefault(3, 0L))
                .fourStar(countsMap.getOrDefault(4, 0L))
                .fiveStar(countsMap.getOrDefault(5, 0L))
                .build();
    }
}
