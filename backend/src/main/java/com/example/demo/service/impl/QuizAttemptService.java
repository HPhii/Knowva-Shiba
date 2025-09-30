package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.*;
import com.example.demo.model.enums.ActivityType;
import com.example.demo.model.io.dto.QuestionReview;
import com.example.demo.model.io.dto.QuizAnswerDTO;
import com.example.demo.model.io.dto.QuizAttemptDetailResponse;
import com.example.demo.model.io.dto.QuizQuestionDTO;
import com.example.demo.model.io.request.quiz.UserAnswerRequest;
import com.example.demo.model.io.response.object.quiz.StartQuizResponse;
import com.example.demo.repository.QuizAttemptRepository;
import com.example.demo.repository.QuizSetRepository;
import com.example.demo.repository.UserAnswerRepository;
import com.example.demo.service.intface.IQuizAttemptService;
import com.example.demo.service.intface.IActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizAttemptService implements IQuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizSetRepository quizSetRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final IActivityLogService activityLogService;

    @Override
    public StartQuizResponse startQuiz(Long quizSetId, User user) {
        QuizSet quizSet = quizSetRepository.findById(quizSetId)
                .orElseThrow(() -> new EntityNotFoundException("QuizSet not found"));
        QuizAttempt attempt = QuizAttempt.builder()
                .quizSet(quizSet)
                .user(user)
                .startedAt(LocalDateTime.now())
                .build();
        quizAttemptRepository.save(attempt);

        List<QuizQuestionDTO> questionDTOs = quizSet.getQuestions().stream()
                .map(this::mapToQuizQuestionDTO)
                .collect(Collectors.toList());

        return new StartQuizResponse(attempt, questionDTOs);
    }

    private QuizQuestionDTO mapToQuizQuestionDTO(QuizQuestion question) {
        QuizQuestionDTO dto = new QuizQuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionHtml(question.getQuestionHtml());
        dto.setImageUrl(question.getImageUrl());
        dto.setTimeLimit(question.getTimeLimit());
        dto.setOrder(question.getOrder());
        dto.setAnswers(question.getAnswers().stream()
                .map(this::mapToQuizAnswerDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    private QuizAnswerDTO mapToQuizAnswerDTO(QuizAnswer answer) {
        QuizAnswerDTO dto = new QuizAnswerDTO();
        dto.setId(answer.getId());
        dto.setAnswerText(answer.getAnswerText());
        return dto;
    }

    @Override
    public QuizAttempt submitQuiz(Long attemptId, List<UserAnswerRequest> userAnswers) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("QuizAttempt not found"));

        for (UserAnswerRequest ua : userAnswers) {
            QuizQuestion question = attempt.getQuizSet().getQuestions().stream()
                    .filter(q -> q.getId().equals(ua.getQuestionId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Question not found"));

            QuizAnswer selectedAnswer = question.getAnswers().stream()
                    .filter(a -> a.getId().equals(ua.getSelectedAnswerId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

            UserAnswer userAnswer = UserAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .selectedAnswer(selectedAnswer)
                    .answeredAt(LocalDateTime.now())
                    .build();
            userAnswerRepository.save(userAnswer);
        }

        int correctAnswers = 0;
        for (UserAnswer ua : attempt.getUserAnswers()) {
            if (ua.getSelectedAnswer().getIsCorrect()) {
                correctAnswers++;
            }
        }
        float score = (float) correctAnswers / attempt.getQuizSet().getQuestions().size() * 100;
        attempt.setScore(score);
        attempt.setCompletedAt(LocalDateTime.now());

        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        // === GHI LOG HOẠT ĐỘNG ===
        String description = String.format("%.1f", score); // Chỉ giữ score cho frontend
        activityLogService.logActivity(attempt.getUser(), ActivityType.ATTEMPT_QUIZ, description, attempt.getQuizSet().getId());
        // =========================

        return savedAttempt;
    }

    @Override
    public QuizAttemptDetailResponse getAttemptDetails(Long attemptId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("QuizAttempt not found"));

        Map<Long, UserAnswer> uniqueAnswers = new HashMap<>();
        for (UserAnswer ua : attempt.getUserAnswers()) {
            uniqueAnswers.putIfAbsent(ua.getQuestion().getId(), ua);
        }

        List<QuestionReview> reviews = new ArrayList<>();
        for (UserAnswer ua : uniqueAnswers.values()) {
            QuizQuestion question = ua.getQuestion();
            QuizAnswer selectedAnswer = ua.getSelectedAnswer();
            QuizAnswer correctAnswer = question.getAnswers().stream()
                    .filter(QuizAnswer::getIsCorrect)
                    .findFirst()
                    .orElse(null);

            // Map danh sách answers thành QuizAnswerDTO
            List<QuizAnswerDTO> answerDTOs = question.getAnswers().stream()
                    .map(a -> new QuizAnswerDTO(a.getId(), a.getAnswerText()))
                    .collect(Collectors.toList());

            reviews.add(new QuestionReview(
                    question.getId(),
                    selectedAnswer.getId(),
                    correctAnswer != null ? correctAnswer.getId() : null,
                    selectedAnswer.getIsCorrect(),
                    answerDTOs
            ));
        }

        return new QuizAttemptDetailResponse(
                attempt.getId(),
                attempt.getScore(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                reviews
        );
    }
}
