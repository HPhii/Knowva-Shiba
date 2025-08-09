package com.example.demo.controller;

import com.example.demo.model.io.request.CreateCommentRequest;
import com.example.demo.model.io.request.CreateRatingRequest;
import com.example.demo.model.io.request.UpdateCommentRequest;
import com.example.demo.model.io.response.CommentResponse;
import com.example.demo.model.io.response.InteractionSummaryResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "16. Interaction Management")
public class InteractionController {

    private final IInteractionService interactionService;
    private final IAccountService accountService;

    @PostMapping("/{entityType}/{entityId}/rating")
    @Operation(
        summary = "Thêm đánh giá cho nội dung",
        description = "Cho phép người dùng đánh giá (1-5 sao) cho quiz set, flashcard set hoặc blog post"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đánh giá được thêm thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc người dùng đã đánh giá trước đó"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nội dung cần đánh giá")
    })
    public ResponseEntity<String> addRating(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId,
            @Valid @RequestBody CreateRatingRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        interactionService.addRating(entityType, entityId, request, userId);
        return ResponseEntity.ok("Đánh giá đã được thêm thành công");
    }

    @PutMapping("/{entityType}/{entityId}/rating")
    @Operation(
        summary = "Cập nhật đánh giá",
        description = "Cho phép người dùng cập nhật đánh giá đã có cho nội dung"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đánh giá được cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá hoặc nội dung")
    })
    public ResponseEntity<String> updateRating(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId,
            @Valid @RequestBody CreateRatingRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        interactionService.updateRating(entityType, entityId, request, userId);
        return ResponseEntity.ok("Đánh giá đã được cập nhật thành công");
    }

    @DeleteMapping("/{entityType}/{entityId}/rating")
    @Operation(
        summary = "Xóa đánh giá",
        description = "Cho phép người dùng xóa đánh giá của mình"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Đánh giá được xóa thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy đánh giá")
    })
    public ResponseEntity<String> deleteRating(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        interactionService.deleteRating(entityType, entityId, userId);
        return ResponseEntity.ok("Đánh giá đã được xóa thành công");
    }

    @GetMapping("/{entityType}/{entityId}/rating/my")
    @Operation(
        summary = "Lấy đánh giá của tôi",
        description = "Lấy đánh giá hiện tại của người dùng đang đăng nhập cho nội dung cụ thể"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy đánh giá thành công (null nếu chưa đánh giá)"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nội dung")
    })
    public ResponseEntity<Integer> getUserRating(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        Integer userRating = interactionService.getUserRating(entityType, entityId, userId);
        return ResponseEntity.ok(userRating);
    }

    @PostMapping("/{entityType}/{entityId}/comment")
    @Operation(
        summary = "Thêm bình luận",
        description = "Thêm bình luận mới cho quiz set, flashcard set hoặc blog post (có thể đính kèm ảnh)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bình luận được thêm thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nội dung hoặc bình luận cha (nếu là reply)")
    })
    public ResponseEntity<CommentResponse> addComment(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId,
            @Valid @RequestBody CreateCommentRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        CommentResponse response = interactionService.addComment(entityType, entityId, request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comment/{commentId}")
    @Operation(
        summary = "Cập nhật bình luận",
        description = "Cập nhật nội dung và ảnh đính kèm của bình luận (chỉ chủ sở hữu mới có thể chỉnh sửa)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bình luận được cập nhật thành công"),
        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc bình luận đã bị xóa"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền chỉnh sửa bình luận này"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
    })
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "ID của bình luận") @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        CommentResponse response = interactionService.updateComment(commentId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comment/{commentId}")
    @Operation(
        summary = "Xóa bình luận",
        description = "Xóa mềm bình luận (chỉ chủ sở hữu mới có thể xóa)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bình luận được xóa thành công"),
        @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
        @ApiResponse(responseCode = "403", description = "Không có quyền xóa bình luận này"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận")
    })
    public ResponseEntity<String> deleteComment(
            @Parameter(description = "ID của bình luận") @PathVariable Long commentId) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        interactionService.deleteComment(commentId, userId);
        return ResponseEntity.ok("Bình luận đã được xóa thành công");
    }

    @GetMapping("/{entityType}/{entityId}/comments")
    @Operation(
        summary = "Lấy danh sách bình luận",
        description = "Lấy danh sách bình luận chính (không bao gồm replies) với phân trang, sắp xếp theo thời gian tạo mới nhất"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách bình luận thành công"),
        @ApiResponse(responseCode = "400", description = "Loại nội dung không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nội dung")
    })
    public ResponseEntity<Page<CommentResponse>> getComments(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<CommentResponse> comments = interactionService.getComments(entityType, entityId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/comment/{parentCommentId}/replies")
    @Operation(
        summary = "Lấy danh sách phản hồi",
        description = "Lấy danh sách replies cho một bình luận cụ thể với phân trang, sắp xếp theo thời gian tạo cũ nhất"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy danh sách phản hồi thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy bình luận cha")
    })
    public ResponseEntity<Page<CommentResponse>> getReplies(
            @Parameter(description = "ID của bình luận cha") @PathVariable Long parentCommentId,
            @PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {
        Page<CommentResponse> replies = interactionService.getReplies(parentCommentId, pageable);
        return ResponseEntity.ok(replies);
    }

    // ==================== SUMMARY ENDPOINT ====================

    @GetMapping("/{entityType}/{entityId}/summary")
    @Operation(
        summary = "Lấy thống kê tương tác",
        description = "Lấy thống kê tổng hợp về đánh giá và bình luận cho nội dung, bao gồm điểm trung bình, phân phối đánh giá và số lượng bình luận"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công"),
        @ApiResponse(responseCode = "400", description = "Loại nội dung không hợp lệ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nội dung")
    })
    public ResponseEntity<InteractionSummaryResponse> getInteractionSummary(
            @Parameter(description = "Loại nội dung (quizset, flashcardset, blogpost)") @PathVariable String entityType,
            @Parameter(description = "ID của nội dung") @PathVariable Long entityId) {
        InteractionSummaryResponse summary = interactionService.getInteractionSummary(entityType, entityId);
        return ResponseEntity.ok(summary);
    }
}
