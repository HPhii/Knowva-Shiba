package com.example.demo.service.strategy;

import com.example.demo.model.entity.Comment;
import com.example.demo.model.entity.Rating;

public interface EntityInteractionStrategy {
    String getEntityType();
    void setEntityForRating(Rating rating, Long entityId);
    void setEntityForComment(Comment comment, Long entityId);
    Object findEntityById(Long entityId);
}
