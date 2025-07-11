package com.example.demo.controller;

import com.example.demo.model.entity.flashcard.Flashcard;
import com.example.demo.model.entity.flashcard.FlashcardAttempt;
import com.example.demo.model.io.dto.PerformanceStats;
import com.example.demo.model.io.dto.SpacedRepetitionModeData;
import com.example.demo.model.io.dto.StudyProgressStats;
import com.example.demo.service.intface.ISpacedRepetitionService;
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
@RequestMapping("/api/spaced-repetition")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "10. Spaced Repetition")
public class SpacedRepetitionController {
    private final ISpacedRepetitionService spacedRepetitionService;

    @GetMapping("/mode-data")
    @Operation(summary = "Lấy dữ liệu cho chế độ học", description = "Lấy thông tin ban đầu khi vào chế độ học lặp lại ngắt quãng, bao gồm cài đặt và số lượng thẻ cần học.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = SpacedRepetitionModeData.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng hoặc bộ thẻ")
    })
    public ResponseEntity<SpacedRepetitionModeData> getSpacedRepetitionModeData(
            @Parameter(description = "ID của người dùng", required = true) @RequestParam Long userId,
            @Parameter(description = "ID của bộ thẻ học", required = true) @RequestParam Long flashcardSetId) {
        SpacedRepetitionModeData modeData = spacedRepetitionService.getModeData(userId, flashcardSetId);
        return ResponseEntity.ok(modeData);
    }

    @PostMapping("/set-new-flashcards-per-day")
    @Operation(summary = "Cài đặt số thẻ mới mỗi ngày", description = "Cho phép người dùng cài đặt số lượng thẻ mới họ muốn học mỗi ngày cho một bộ thẻ cụ thể.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cài đặt thành công"),
            @ApiResponse(responseCode = "400", description = "Số thẻ không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng hoặc bộ thẻ")
    })
    public ResponseEntity<Void> setNewFlashcardsPerDay(
            @Parameter(description = "ID của người dùng", required = true) @RequestParam Long userId,
            @Parameter(description = "ID của bộ thẻ học", required = true) @RequestParam Long flashcardSetId,
            @Parameter(description = "Số lượng thẻ mới muốn học mỗi ngày", required = true) @RequestParam Integer newFlashcardsPerDay) {
        spacedRepetitionService.setNewFlashcardsPerDay(userId, flashcardSetId, newFlashcardsPerDay);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/start-session")
    @Operation(summary = "Bắt đầu một phiên học", description = "Lấy danh sách các thẻ cần ôn tập cho phiên học hiện tại, bao gồm cả thẻ mới và thẻ đến hạn ôn tập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công, trả về danh sách thẻ cần học"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng hoặc bộ thẻ"),
            @ApiResponse(responseCode = "500", description = "Không có thẻ nào để học")
    })
    public ResponseEntity<List<Flashcard>> startStudySession(
            @Parameter(description = "ID của người dùng", required = true) @RequestParam Long userId,
            @Parameter(description = "ID của bộ thẻ học", required = true) @RequestParam Long flashcardSetId) {
        List<Flashcard> flashcards = spacedRepetitionService.startStudySession(userId, flashcardSetId);
        return ResponseEntity.ok(flashcards);
    }

    @PostMapping("/submit-review")
    @Operation(summary = "Gửi kết quả ôn tập một thẻ", description = "Người dùng gửi kết quả đánh giá mức độ ghi nhớ của một thẻ (quality) để hệ thống cập nhật lại lịch ôn tập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gửi kết quả thành công, trả về thống kê tiến độ học tập",
                    content = @Content(schema = @Schema(implementation = StudyProgressStats.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy người dùng hoặc thẻ")
    })
    public ResponseEntity<StudyProgressStats> submitReview(
            @Parameter(description = "ID của người dùng", required = true) @RequestParam Long userId,
            @Parameter(description = "ID của thẻ vừa ôn tập", required = true) @RequestParam Long flashcardId,
            @Parameter(description = "ID của bộ thẻ", required = true) @RequestParam Long flashcardSetId,
            @Parameter(description = "Mức độ ghi nhớ (0-5), với 5 là nhớ rất rõ", required = true) @RequestParam int quality) {
        StudyProgressStats stats = spacedRepetitionService.submitReview(userId, flashcardId, flashcardSetId, quality);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/study-history")
    @Operation(summary = "Lấy lịch sử học tập", description = "Lấy danh sách các lần ôn tập (attempts) của người dùng cho một bộ thẻ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công, trả về lịch sử ôn tập")
    })
    public ResponseEntity<List<FlashcardAttempt>> getStudyHistory(
            @Parameter(description = "ID của người dùng", required = true) @RequestParam Long userId,
            @Parameter(description = "ID của bộ thẻ học", required = true) @RequestParam Long flashcardSetId) {
        List<FlashcardAttempt> attempts = spacedRepetitionService.getStudyHistory(userId, flashcardSetId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/performance-stats")
    @Operation(summary = "Lấy thống kê hiệu suất học tập", description = "Lấy các chỉ số thống kê về hiệu suất học tập như tỷ lệ ghi nhớ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = PerformanceStats.class)))
    })
    public ResponseEntity<PerformanceStats> getPerformanceStats(
            @Parameter(description = "ID của người dùng", required = true) @RequestParam Long userId,
            @Parameter(description = "ID của bộ thẻ học", required = true) @RequestParam Long flashcardSetId) {
        PerformanceStats stats = spacedRepetitionService.getPerformanceStats(userId, flashcardSetId);
        return ResponseEntity.ok(stats);
    }
}
