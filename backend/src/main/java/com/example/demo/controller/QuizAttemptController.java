package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAttempt;
import com.example.demo.model.io.dto.QuizAttemptDetailResponse;
import com.example.demo.model.io.request.quiz.UserAnswerRequest;
import com.example.demo.model.io.response.object.quiz.StartQuizResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizAttemptService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "api")
public class QuizAttemptController {
    private final IQuizAttemptService quizAttemptService;
    private final IAccountService accountService;

    @GetMapping("/{quizSetId}/start")
    public ResponseEntity<StartQuizResponse> startQuiz(@PathVariable Long quizSetId) {
        User user = accountService.getCurrentAccount().getUser();
        StartQuizResponse attempt = quizAttemptService.startQuiz(quizSetId, user);
        return ResponseEntity.ok(attempt);
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<QuizAttempt> submitQuiz(
            @PathVariable Long attemptId,
            @RequestBody List<UserAnswerRequest> userAnswers) {
        QuizAttempt attempt = quizAttemptService.submitQuiz(attemptId, userAnswers);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/{attemptId}/review")
    public ResponseEntity<QuizAttemptDetailResponse> getAttemptDetails(@PathVariable Long attemptId) {
        QuizAttemptDetailResponse response = quizAttemptService.getAttemptDetails(attemptId);
        return ResponseEntity.ok(response);
    }
}
