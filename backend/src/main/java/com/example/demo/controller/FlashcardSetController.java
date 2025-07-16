package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Category;
import com.example.demo.model.io.request.InviteUserRequest;
import com.example.demo.model.io.request.flashcard.CreateFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.SaveFlashcardSetRequest;
import com.example.demo.model.io.request.flashcard.SubmitExamModeRequest;
import com.example.demo.model.io.request.flashcard.UpdateFlashcardSetRequest;
import com.example.demo.model.io.response.object.flashcard.ExamModeFeedbackResponse;
import com.example.demo.model.io.response.object.flashcard.FlashcardSetResponse;
import com.example.demo.model.io.response.object.flashcard.SimplifiedFlashcardSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IFlashcardSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/flashcard-sets")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "9. Flashcard Set Management")
public class FlashcardSetController {
    private final IFlashcardSetService flashcardSetService;
    private final IAccountService accountService;

    @PostMapping(value = "/generate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Tạo Flashcard Set từ AI (POSTMAN)", description = "Tải lên văn bản hoặc file để AI tự động tạo ra một Flashcard Set.")
    public ResponseEntity<SimplifiedFlashcardSetResponse> generateFlashcardSet(
            @Parameter(description = "Thông tin cài đặt cho Flashcard Set") @RequestPart("flashcardSet") CreateFlashcardSetRequest flashcardSetRequest,
            @Parameter(description = "Danh sách file để AI phân tích") @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(description = "Đoạn văn bản để AI phân tích") @RequestPart(value = "text", required = false) String inputText) {
        SimplifiedFlashcardSetResponse response = flashcardSetService.generateFlashcardSet(flashcardSetRequest, files, inputText);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    @Operation(summary = "Lưu một Flashcard Set mới", description = "Lưu Flashcard Set được tạo thủ công hoặc từ AI vào cơ sở dữ liệu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lưu thành công",
                    content = @Content(schema = @Schema(implementation = FlashcardSetResponse.class)))
    })
    public ResponseEntity<FlashcardSetResponse> saveFlashcardSet(@RequestBody SaveFlashcardSetRequest request) {
        FlashcardSetResponse response = flashcardSetService.saveFlashcardSet(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{flashcardSetId}")
    @Operation(summary = "Cập nhật một Flashcard Set", description = "Cập nhật thông tin và các thẻ trong một bộ đã có.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = FlashcardSetResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền chỉnh sửa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Flashcard Set")
    })
    public ResponseEntity<FlashcardSetResponse> updateFlashcardSet(
            @Parameter(description = "ID của Flashcard Set cần cập nhật") @PathVariable Long flashcardSetId,
            @RequestBody UpdateFlashcardSetRequest request,
            @Parameter(description = "Access token cho Flashcard Set ẩn") @RequestParam(required = false) String token) {
        FlashcardSetResponse response = flashcardSetService.updateFlashcardSet(flashcardSetId, request, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một Flashcard Set", description = "Lấy thông tin đầy đủ của một Flashcard Set. Cần access token cho Flashcard Set ẩn.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = FlashcardSetResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Flashcard Set")
    })
    public ResponseEntity<FlashcardSetResponse> getFlashcardSetById(
            @Parameter(description = "ID của Flashcard Set") @PathVariable Long id,
            @Parameter(description = "Access token cho Flashcard Set ẩn") @RequestParam(required = false) String token) {
        FlashcardSetResponse response = flashcardSetService.getFlashcardSetById(id, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một Flashcard Set", description = "Chỉ chủ sở hữu mới có quyền xóa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Flashcard Set")
    })
    public ResponseEntity<FlashcardSetResponse> deleteFlashcardSetById(@PathVariable Long id) {
        FlashcardSetResponse response = flashcardSetService.deleteFlashcardSetById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy các Flashcard Set của một người dùng", description = "Lấy danh sách các Flashcard Set do một người dùng cụ thể tạo ra.")
    public ResponseEntity<List<FlashcardSetResponse>> getFlashcardSetsOfUser(@Parameter(description = "ID của người dùng") @PathVariable Long userId) {
        List<FlashcardSetResponse> responses = flashcardSetService.getFlashcardSetsOfUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-flashcard-sets")
    @Operation(summary = "Lấy các Flashcard Set của tôi", description = "Lấy danh sách các Flashcard Set do người dùng đang đăng nhập tạo ra.")
    public ResponseEntity<List<FlashcardSetResponse>> getMyFlashcardSets() {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<FlashcardSetResponse> responses = flashcardSetService.getFlashcardSetsOfUser(currentUser.getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả Flashcard Set công khai", description = "Lấy danh sách các Flashcard Set có thể truy cập (công khai, được mời).")
    public ResponseEntity<List<FlashcardSetResponse>> getAllFlashcardSets() {
        List<FlashcardSetResponse> responses = flashcardSetService.getAllFlashcardSets();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{flashcardSetId}/exam-mode/submit")
    @Operation(summary = "Nộp bài chế độ kiểm tra", description = "Gửi câu trả lời của người dùng cho một thẻ trong chế độ kiểm tra để AI chấm điểm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chấm điểm thành công",
                    content = @Content(schema = @Schema(implementation = ExamModeFeedbackResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Flashcard Set hoặc thẻ")
    })
    public ResponseEntity<ExamModeFeedbackResponse> submitExamMode(
            @Parameter(description = "ID của Flashcard Set") @PathVariable Long flashcardSetId,
            @RequestBody SubmitExamModeRequest request) {
        ExamModeFeedbackResponse response = flashcardSetService.submitExamMode(flashcardSetId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{flashcardSetId}/generate-quiz")
    @Operation(summary = "Tạo quiz từ Flashcard Set", description = "Dùng AI để tự động tạo một bài kiểm tra trắc nghiệm từ các thẻ trong bộ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo quiz thành công",
                    content = @Content(schema = @Schema(implementation = SimplifiedQuizSetResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Flashcard Set")
    })
    public ResponseEntity<SimplifiedQuizSetResponse> generateQuizMode(
            @Parameter(description = "ID của Flashcard Set") @PathVariable Long flashcardSetId,
            @Parameter(description = "Ngôn ngữ cho câu hỏi") @RequestParam(defaultValue = "en") String language,
            @Parameter(description = "Loại câu hỏi (MULTIPLE_CHOICE, TRUE_FALSE, MIXED)") @RequestParam(defaultValue = "MULTIPLE_CHOICE") String questionType,
            @Parameter(description = "Số lượng câu hỏi tối đa") @RequestParam(defaultValue = "5") int maxQuestions) {
        SimplifiedQuizSetResponse response = flashcardSetService.generateQuizMode(flashcardSetId, language, questionType, maxQuestions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Mời người dùng vào Flashcard Set", description = "Chủ sở hữu mời người dùng khác vào xem hoặc chỉnh sửa Flashcard Set riêng tư.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mời thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền mời"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Flashcard Set hoặc người được mời")
    })
    public ResponseEntity<String> inviteUserToFlashcardSet(
            @Parameter(description = "ID của Flashcard Set") @PathVariable Long id,
            @RequestBody InviteUserRequest request) {
        flashcardSetService.inviteUserToFlashcardSet(id, request.getUserId(), request.getPermission());
        return ResponseEntity.ok("User invited successfully");
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Lấy các Flashcard Set theo danh mục", description = "Lấy danh sách các Flashcard Set công khai thuộc một danh mục.")
    public ResponseEntity<List<FlashcardSetResponse>> getFlashcardSetsByCategory(
            @Parameter(description = "Tên danh mục") @PathVariable Category category) {
        List<FlashcardSetResponse> responses = flashcardSetService.getFlashcardSetsByCategory(category);
        return ResponseEntity.ok(responses);
    }
}