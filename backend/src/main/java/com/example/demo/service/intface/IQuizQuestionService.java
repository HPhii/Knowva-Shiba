package com.example.demo.service.intface;

import com.example.demo.model.entity.quiz.QuizQuestion;
import com.example.demo.model.entity.quiz.QuizSet;
import com.example.demo.model.enums.QuestionType;

import java.util.List;

public interface IQuizQuestionService {
    List<QuizQuestion> generateQuestionsFromContent(String content, QuestionType type, int max, QuizSet quizSet);
}

