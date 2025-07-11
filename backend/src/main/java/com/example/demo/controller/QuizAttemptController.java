package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.quiz.QuizAttempt;
import com.example.demo.model.io.dto.QuizAttemptDetailResponse;
import com.example.demo.model.io.request.quiz.UserAnswerRequest;
import com.example.demo.model.io.response.object.quiz.StartQuizResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@CrossOrigin("*")
@SecurityRequirement(name = "api")
@Tag(name = "8. Quiz Attempt Management")
public class QuizAttemptController {
    private final IQuizAttemptService quizAttemptService;
    private final IAccountService accountService;

    @GetMapping("/{quizSetId}/start")
    @Operation(summary = "Bắt đầu làm bài kiểm tra", description = "Tạo một lần làm bài (attempt) mới cho một bộ câu hỏi và trả về danh sách câu hỏi.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bắt đầu thành công",
                    content = @Content(schema = @Schema(implementation = StartQuizResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bộ câu hỏi (QuizSet not found)")
    })
    public ResponseEntity<StartQuizResponse> startQuiz(
            @Parameter(description = "ID của bộ câu hỏi để bắt đầu", required = true) @PathVariable Long quizSetId) {
        User user = accountService.getCurrentAccount().getUser();
        StartQuizResponse attempt = quizAttemptService.startQuiz(quizSetId, user);
        return ResponseEntity.ok(attempt);
    }

    @PostMapping("/{attemptId}/submit")
    @Operation(summary = "Nộp bài kiểm tra", description = "Gửi các câu trả lời của người dùng, hệ thống sẽ chấm điểm và lưu kết quả.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nộp bài thành công, trả về kết quả lần làm bài",
                    content = @Content(schema = @Schema(implementation = QuizAttempt.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lần làm bài, câu hỏi hoặc câu trả lời")
    })
    public ResponseEntity<QuizAttempt> submitQuiz(
            @Parameter(description = "ID của lần làm bài đang thực hiện", required = true) @PathVariable Long attemptId,
            @RequestBody List<UserAnswerRequest> userAnswers) {
        QuizAttempt attempt = quizAttemptService.submitQuiz(attemptId, userAnswers);
        return ResponseEntity.ok(attempt);
    }

    @GetMapping("/{attemptId}/review")
    @Operation(summary = "Xem lại bài làm", description = "Lấy thông tin chi tiết về một lần làm bài đã hoàn thành, bao gồm câu trả lời của người dùng và đáp án đúng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết thành công",
                    content = @Content(schema = @Schema(implementation = QuizAttemptDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy lần làm bài")
    })
    public ResponseEntity<QuizAttemptDetailResponse> getAttemptDetails(
            @Parameter(description = "ID của lần làm bài cần xem lại", required = true) @PathVariable Long attemptId) {
        QuizAttemptDetailResponse response = quizAttemptService.getAttemptDetails(attemptId);
        return ResponseEntity.ok(response);
    }
}