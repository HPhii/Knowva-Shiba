package com.example.demo.controller;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Category;
import com.example.demo.model.io.request.InviteUserRequest;
import com.example.demo.model.io.request.quiz.CreateQuizSetRequest;
import com.example.demo.model.io.request.quiz.SaveQuizSetRequest;
import com.example.demo.model.io.request.quiz.UpdateQuizSetRequest;
import com.example.demo.model.io.response.object.quiz.QuizSetResponse;
import com.example.demo.model.io.response.object.quiz.SimplifiedQuizSetResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IQuizSetService;
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
@RequestMapping("/api/quiz-sets")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "7. Quiz Set Management")
public class QuizSetController {

    private final IQuizSetService quizSetService;
    private final IAccountService accountService;

    @PostMapping(value = "/generate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Tạo Quiz Set từ AI (POSTMAN)", description = "Tải lên văn bản hoặc file (PDF, hình ảnh) để AI tự động tạo ra một Quiz Set trắc nghiệm.")
    public ResponseEntity<SimplifiedQuizSetResponse> generateQuizSet(
            @Parameter(description = "Đối tượng chứa thông tin cài đặt cho Quiz Set") @RequestPart("quizSet") CreateQuizSetRequest quizSetRequest,
            @Parameter(description = "Danh sách các file (ảnh, pdf) để AI phân tích") @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(description = "Đoạn văn bản để AI phân tích") @RequestPart(value = "text", required = false) String inputText) {
        SimplifiedQuizSetResponse response = quizSetService.generateQuizSet(quizSetRequest, files, inputText);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    @Operation(summary = "Lưu một Quiz Set mới", description = "Lưu một Quiz Set được tạo thủ công hoặc từ AI vào cơ sở dữ liệu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lưu thành công",
                    content = @Content(schema = @Schema(implementation = QuizSetResponse.class)))
    })
    public ResponseEntity<QuizSetResponse> saveQuizSet(@RequestBody SaveQuizSetRequest request) {
        QuizSetResponse response = quizSetService.saveQuizSet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy các Quiz Set của một người dùng", description = "Lấy danh sách tất cả các Quiz Set do một người dùng cụ thể tạo ra.")
    public ResponseEntity<List<QuizSetResponse>> getQuizSetsOfUser(
            @Parameter(description = "ID của người dùng") @PathVariable Long userId) {
        List<QuizSetResponse> responses = quizSetService.getQuizSetsOfUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-quiz-sets")
    @Operation(summary = "Lấy các Quiz Set của tôi", description = "Lấy danh sách tất cả các Quiz Set do người dùng đang đăng nhập tạo ra.")
    public ResponseEntity<List<QuizSetResponse>> getMyQuizSets() {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<QuizSetResponse> responses = quizSetService.getQuizSetsOfUser(currentUser.getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả Quiz Set công khai", description = "Lấy danh sách tất cả các Quiz Set có thể truy cập (công khai, hoặc được mời).")
    public ResponseEntity<List<QuizSetResponse>> getAllQuizSets() {
        List<QuizSetResponse> responses = quizSetService.getAllQuizSets();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một Quiz Set bằng ID", description = "Lấy thông tin đầy đủ của một Quiz Set. Đối với Quiz Set ẩn, cần có access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = QuizSetResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Quiz Set")
    })
    public ResponseEntity<QuizSetResponse> getQuizSetById(
            @Parameter(description = "ID của Quiz Set") @PathVariable Long id,
            @Parameter(description = "Access token cho các Quiz Set có link mới xem được (visibility = HIDDEN)") @RequestParam(required = false) String token) {
        QuizSetResponse response = quizSetService.getQuizSetById(id, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một Quiz Set", description = "Chỉ chủ sở hữu mới có quyền xóa Quiz Set của mình.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Quiz Set")
    })
    public ResponseEntity<QuizSetResponse> deleteQuizSetById(@PathVariable Long id) {
        QuizSetResponse response = quizSetService.deleteQuizSetById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{quizSetId}")
    @Operation(summary = "Cập nhật một Quiz Set", description = "Cập nhật thông tin và các câu hỏi của một Quiz Set đã có.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = QuizSetResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền chỉnh sửa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Quiz Set hoặc câu hỏi con")
    })
    public ResponseEntity<QuizSetResponse> updateQuizSet(
            @Parameter(description = "ID của Quiz Set cần cập nhật") @PathVariable Long quizSetId,
            @RequestBody UpdateQuizSetRequest request,
            @Parameter(description = "Access token nếu cần để chỉnh sửa Quiz Set ẩn") @RequestParam(required = false) String token) {
        QuizSetResponse response = quizSetService.updateQuizSet(quizSetId, request, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Mời người dùng vào Quiz Set", description = "Chủ sở hữu mời một người dùng khác vào xem hoặc chỉnh sửa Quiz Set riêng tư.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mời thành công"),
            @ApiResponse(responseCode = "403", description = "Chỉ chủ sở hữu mới có quyền mời"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy Quiz Set hoặc người được mời")
    })
    public ResponseEntity<String> inviteUserToQuizSet(
            @Parameter(description = "ID của Quiz Set") @PathVariable Long id,
            @RequestBody InviteUserRequest request) {
        quizSetService.inviteUserToQuizSet(id, request.getUserId(), request.getPermission());
        return ResponseEntity.ok("User invited successfully");
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Lấy các Quiz Set theo category", description = "Lấy danh sách các Quiz Set công khai thuộc một category cụ thể.")
    public ResponseEntity<List<QuizSetResponse>> getQuizSetsByCategory(
            @Parameter(description = "Tên category") @PathVariable Category category) {
        List<QuizSetResponse> responses = quizSetService.getQuizSetsByCategory(category);
        return ResponseEntity.ok(responses);
    }
}