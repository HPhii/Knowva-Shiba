package com.example.demo.service.impl;

import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.QuestionType;
import com.example.demo.service.intface.IQuizQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class QuizQuestionService implements IQuizQuestionService {
    @Override
    public List<QuizQuestion> generateQuestionsFromContent(String content, QuestionType type, int max, QuizSet quizSet) {
        return List.of();
    }
}
