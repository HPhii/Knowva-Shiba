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
    @Operation(summary = "Tạo bộ câu hỏi từ AI", description = "Tải lên văn bản hoặc file (PDF, hình ảnh) để AI tự động tạo ra một bộ câu hỏi trắc nghiệm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công, trả về bộ câu hỏi đã được đơn giản hóa",
                    content = @Content(schema = @Schema(implementation = SimplifiedQuizSetResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ")
    })
    public ResponseEntity<SimplifiedQuizSetResponse> generateQuizSet(
            @Parameter(description = "Đối tượng chứa thông tin cài đặt cho bộ câu hỏi") @RequestPart("quizSet") CreateQuizSetRequest quizSetRequest,
            @Parameter(description = "Danh sách các file (ảnh, pdf) để AI phân tích") @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(description = "Đoạn văn bản để AI phân tích") @RequestPart(value = "text", required = false) String inputText) {
        SimplifiedQuizSetResponse response = quizSetService.generateQuizSet(quizSetRequest, files, inputText);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/save")
    @Operation(summary = "Lưu một bộ câu hỏi mới", description = "Lưu một bộ câu hỏi được tạo thủ công hoặc từ AI vào cơ sở dữ liệu.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lưu thành công",
                    content = @Content(schema = @Schema(implementation = QuizSetResponse.class)))
    })
    public ResponseEntity<QuizSetResponse> saveQuizSet(@RequestBody SaveQuizSetRequest request) {
        QuizSetResponse response = quizSetService.saveQuizSet(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy các bộ câu hỏi của một người dùng", description = "Lấy danh sách tất cả các bộ câu hỏi do một người dùng cụ thể tạo ra.")
    public ResponseEntity<List<QuizSetResponse>> getQuizSetsOfUser(
            @Parameter(description = "ID của người dùng") @PathVariable Long userId) {
        List<QuizSetResponse> responses = quizSetService.getQuizSetsOfUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my-quiz-sets")
    @Operation(summary = "Lấy các bộ câu hỏi của tôi", description = "Lấy danh sách tất cả các bộ câu hỏi do người dùng đang đăng nhập tạo ra.")
    public ResponseEntity<List<QuizSetResponse>> getMyQuizSets() {
        User currentUser = accountService.getCurrentAccount().getUser();
        List<QuizSetResponse> responses = quizSetService.getQuizSetsOfUser(currentUser.getId());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    @Operation(summary = "Lấy tất cả bộ câu hỏi công khai", description = "Lấy danh sách tất cả các bộ câu hỏi có thể truy cập (công khai, hoặc được mời).")
    public ResponseEntity<List<QuizSetResponse>> getAllQuizSets() {
        List<QuizSetResponse> responses = quizSetService.getAllQuizSets();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một bộ câu hỏi bằng ID", description = "Lấy thông tin đầy đủ của một bộ câu hỏi. Đối với bộ câu hỏi ẩn, cần có access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = QuizSetResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bộ câu hỏi")
    })
    public ResponseEntity<QuizSetResponse> getQuizSetById(
            @Parameter(description = "ID của bộ câu hỏi") @PathVariable Long id,
            @Parameter(description = "Access token cho các bộ câu hỏi có link mới xem được (visibility = HIDDEN)") @RequestParam(required = false) String token) {
        QuizSetResponse response = quizSetService.getQuizSetById(id, token);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa một bộ câu hỏi", description = "Chỉ chủ sở hữu mới có quyền xóa bộ câu hỏi của mình.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền xóa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bộ câu hỏi")
    })
    public ResponseEntity<QuizSetResponse> deleteQuizSetById(@PathVariable Long id) {
        QuizSetResponse response = quizSetService.deleteQuizSetById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{quizSetId}")
    @Operation(summary = "Cập nhật một bộ câu hỏi", description = "Cập nhật thông tin và các câu hỏi của một bộ câu hỏi đã có.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = QuizSetResponse.class))),
            @ApiResponse(responseCode = "403", description = "Không có quyền chỉnh sửa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bộ câu hỏi hoặc câu hỏi con")
    })
    public ResponseEntity<QuizSetResponse> updateQuizSet(
            @Parameter(description = "ID của bộ câu hỏi cần cập nhật") @PathVariable Long quizSetId,
            @RequestBody UpdateQuizSetRequest request,
            @Parameter(description = "Access token nếu cần để chỉnh sửa bộ câu hỏi ẩn") @RequestParam(required = false) String token) {
        QuizSetResponse response = quizSetService.updateQuizSet(quizSetId, request, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Mời người dùng vào bộ câu hỏi", description = "Chủ sở hữu mời một người dùng khác vào xem hoặc chỉnh sửa bộ câu hỏi riêng tư.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mời thành công"),
            @ApiResponse(responseCode = "403", description = "Chỉ chủ sở hữu mới có quyền mời"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy bộ câu hỏi hoặc người được mời")
    })
    public ResponseEntity<String> inviteUserToQuizSet(
            @Parameter(description = "ID của bộ câu hỏi") @PathVariable Long id,
            @RequestBody InviteUserRequest request) {
        quizSetService.inviteUserToQuizSet(id, request.getUserId(), request.getPermission());
        return ResponseEntity.ok("User invited successfully");
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Lấy các bộ câu hỏi theo danh mục", description = "Lấy danh sách các bộ câu hỏi công khai thuộc một danh mục cụ thể.")
    public ResponseEntity<List<QuizSetResponse>> getQuizSetsByCategory(
            @Parameter(description = "Tên danh mục") @PathVariable Category category) {
        List<QuizSetResponse> responses = quizSetService.getQuizSetsByCategory(category);
        return ResponseEntity.ok(responses);
    }
}