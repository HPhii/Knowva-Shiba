package com.example.demo.service.intface;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAttempt;
import com.example.demo.model.io.dto.QuizAttemptDetailResponse;
import com.example.demo.model.io.request.quiz.UserAnswerRequest;
import com.example.demo.model.io.response.object.quiz.StartQuizResponse;

import java.util.List;

public interface IQuizAttemptService {
    StartQuizResponse startQuiz(Long quizSetId, User user);
    QuizAttemptDetailResponse getAttemptDetails(Long attemptId);
    QuizAttempt submitQuiz(Long attemptId, List<UserAnswerRequest> userAnswers);
}
