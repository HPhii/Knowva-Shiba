package com.example.demo.controller;

import com.example.demo.model.io.response.paged.PagedAccountResponse;
import com.example.demo.model.io.response.paged.PagedFlashcardSetResponse;
import com.example.demo.model.io.response.paged.PagedQuizSetResponse;
import com.example.demo.service.intface.ISearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "5. Search")
public class SearchController {
    private final ISearchService searchService;

    @GetMapping("/quiz-sets")
    @Operation(summary = "Tìm kiếm bộ câu hỏi (Quiz Sets)", description = "Tìm kiếm các bộ câu hỏi công khai hoặc người dùng có quyền truy cập dựa trên từ khóa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công, trả về danh sách kết quả",
                    content = @Content(schema = @Schema(implementation = PagedQuizSetResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    public ResponseEntity<PagedQuizSetResponse> searchQuizSets(
            @Parameter(description = "Từ khóa để tìm kiếm trong tiêu đề và mô tả", required = true) @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng kết quả mỗi trang") @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedQuizSetResponse quizSets = searchService.searchQuizSets(keyword, pageable);
        return ResponseEntity.ok(quizSets);
    }

    @GetMapping("/flashcard-sets")
    @Operation(summary = "Tìm kiếm bộ thẻ học (Flashcard Sets)", description = "Tìm kiếm các bộ thẻ học công khai hoặc người dùng có quyền truy cập dựa trên từ khóa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công, trả về danh sách kết quả",
                    content = @Content(schema = @Schema(implementation = PagedFlashcardSetResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    public ResponseEntity<PagedFlashcardSetResponse> searchFlashcardSets(
            @Parameter(description = "Từ khóa để tìm kiếm trong tiêu đề và mô tả", required = true) @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng kết quả mỗi trang") @RequestParam(defaultValue = "8") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedFlashcardSetResponse flashcardSets = searchService.searchFlashcardSets(keyword, pageable);
        return ResponseEntity.ok(flashcardSets);
    }

    @GetMapping("/accounts")
    @Operation(summary = "Tìm kiếm tài khoản người dùng", description = "Tìm kiếm tài khoản người dùng dựa trên username hoặc email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công, trả về danh sách tài khoản",
                    content = @Content(schema = @Schema(implementation = PagedAccountResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa xác thực")
    })
    public ResponseEntity<PagedAccountResponse> searchAccounts(
            @Parameter(description = "Từ khóa để tìm kiếm username hoặc email", required = true) @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng kết quả mỗi trang") @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PagedAccountResponse accounts = searchService.searchAccounts(keyword, pageable);
        return ResponseEntity.ok(accounts);
    }
}