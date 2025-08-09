package com.example.demo.service.strategy;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.Comment;
import com.example.demo.model.entity.Rating;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.repository.QuizSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuizSetInteractionStrategy implements EntityInteractionStrategy {

    private final QuizSetRepository quizSetRepository;

    @Override
    public String getEntityType() {
        return "quizset";
    }

    @Override
    public void setEntityForRating(Rating rating, Long entityId) {
        QuizSet quizSet = findEntityById(entityId);
        rating.setQuizSet(quizSet);
    }

    @Override
    public void setEntityForComment(Comment comment, Long entityId) {
        QuizSet quizSet = findEntityById(entityId);
        comment.setQuizSet(quizSet);
    }

    @Override
    public QuizSet findEntityById(Long entityId) {
        return quizSetRepository.findById(entityId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found with id: " + entityId));
    }
}
