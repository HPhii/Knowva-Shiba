package com.example.demo.controller;

import com.example.demo.model.enums.BlogPostStatus;
import com.example.demo.model.io.request.*;
import com.example.demo.model.io.response.object.BlogCategoryResponse;
import com.example.demo.model.io.response.object.BlogPostResponse;
import com.example.demo.model.io.response.paged.PagedBlogPostResponse;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.IBlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@CrossOrigin("*")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Tag(name = "15. Blog Management")
public class BlogController {

    private final IBlogService blogService;
    private final IAccountService accountService;

    @GetMapping("/posts")
    @Operation(summary = "Lấy danh sách bài viết đã xuất bản", description = "Lấy danh sách các bài viết đã được phân trang (chỉ các bài đã PUBLISHED). Hỗ trợ lọc theo danh mục và từ khóa.")
    public ResponseEntity<PagedBlogPostResponse> getPublishedBlogPosts(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử mỗi trang") @RequestParam(defaultValue = "6") int size,
            @Parameter(description = "Sắp xếp theo trường (vd: publishedAt)") @RequestParam(defaultValue = "publishedAt") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Lọc theo ID danh mục") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Lọc theo từ khóa trong tiêu đề") @RequestParam(required = false) String keyword
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(blogService.getAllBlogPosts(BlogPostStatus.PUBLISHED, categoryId, null, keyword, pageable));
    }

    @GetMapping("/posts/slug/{slug}")
    @Operation(summary = "Lấy chi tiết một bài viết bằng Slug", description = "Lấy thông tin chi tiết của một bài viết dựa trên slug của nó, thân thiện cho SEO.")
    public ResponseEntity<BlogPostResponse> getBlogPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogService.getBlogPostBySlug(slug));
    }

    @GetMapping("/user/{userId}/posts")
    @Operation(summary = "Lấy các bài viết đã xuất bản của một tác giả", description = "Lấy danh sách các bài viết đã PUBLISHED của một người dùng cụ thể, có phân trang.")
    public ResponseEntity<PagedBlogPostResponse> getPublishedPostsByAuthor(
            @PathVariable Long userId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return ResponseEntity.ok(blogService.getAllBlogPosts(BlogPostStatus.PUBLISHED, null, userId, null, pageable));
    }

    @GetMapping("/posts/{id}")
    @Operation(summary = "Lấy chi tiết một bài viết bằng ID", description = "Lấy thông tin chi tiết của một bài viết dựa trên ID. (Sử dụng nội bộ hoặc cho các trường hợp đặc biệt)")
    public ResponseEntity<BlogPostResponse> getBlogPostById(@PathVariable Long id) {
        return ResponseEntity.ok(blogService.getBlogPostById(id));
    }

    @PostMapping("/posts")
    @Operation(summary = "[AUTH] Tạo bài viết mới", description = "Tạo một bài viết mới. Có thể lưu nháp (DRAFT) hoặc gửi duyệt (PENDING_APPROVAL).")
    public ResponseEntity<BlogPostResponse> createBlogPost(@Valid @RequestBody CreateBlogPostRequest request) {
        Long authorId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(blogService.createBlogPost(request, authorId));
    }

    @PutMapping("/posts/{id}")
    @Operation(summary = "[AUTH] Cập nhật bài viết", description = "Tác giả cập nhật lại bài viết của chính mình. Chỉ có thể sửa khi là tác giả.")
    public ResponseEntity<BlogPostResponse> updateBlogPost(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBlogPostRequest request) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        return ResponseEntity.ok(blogService.updateBlogPost(id, request, userId));
    }

    @GetMapping("/my-posts")
    @Operation(summary = "[AUTH] Lấy các bài viết của tôi", description = "Lấy danh sách tất cả bài viết của người dùng đang đăng nhập, bao gồm cả bản nháp.")
    public ResponseEntity<PagedBlogPostResponse> getMyPosts(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) BlogPostStatus status
    ) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(blogService.getMyBlogPosts(userId, status, pageable));
    }

    @DeleteMapping("/posts/{id}")
    @Operation(summary = "[AUTH] Xóa một bài viết", description = "Tác giả xóa bài viết của mình, hoặc Admin xóa bài viết bất kỳ.")
    public ResponseEntity<Void> deleteBlogPost(@PathVariable Long id) {
        Long userId = accountService.getCurrentAccount().getUser().getId();
        blogService.deleteBlogPost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    @Operation(summary = "Lấy danh sách danh mục blog", description = "Lấy tất cả các danh mục blog.")
    public ResponseEntity<List<BlogCategoryResponse>> getAllBlogCategories() {
        return ResponseEntity.ok(blogService.getAllBlogCategories());
    }

    // --- ADMIN ENDPOINTS ---

    @GetMapping("/admin/posts")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "api")
    @Operation(summary = "[ADMIN] Lấy bài viết với bộ lọc động", description = "Lấy danh sách các bài viết với bộ lọc theo trạng thái, danh mục, tác giả và từ khóa.")
    public ResponseEntity<PagedBlogPostResponse> getBlogPostsByStatus(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo trường") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection,
            @Parameter(description = "Lọc theo trạng thái") @RequestParam(required = false) BlogPostStatus status,
            @Parameter(description = "Lọc theo ID danh mục") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Lọc theo ID tác giả") @RequestParam(required = false) Long authorId,
            @Parameter(description = "Lọc theo từ khóa trong tiêu đề") @RequestParam(required = false) String keyword
    ) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(blogService.getAllBlogPosts(status, categoryId, authorId, keyword, pageable));
    }

    @PutMapping("/admin/posts/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "api")
    @Operation(summary = "[ADMIN] Cập nhật trạng thái bài viết", description = "Cập nhật trạng thái của bài viết (PUBLISHED, REJECTED, etc.).")
    public ResponseEntity<BlogPostResponse> updateBlogPostStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBlogPostStatusRequest request) {
        return ResponseEntity.ok(blogService.updateBlogPostStatus(id, request.getStatus()));
    }

    @PostMapping("/admin/categories")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Tạo danh mục mới", description = "Tạo một danh mục blog mới.")
    public ResponseEntity<BlogCategoryResponse> createBlogCategory(@Valid @RequestBody CreateBlogCategoryRequest request) {
        return ResponseEntity.ok(blogService.createBlogCategory(request));
    }

    @PutMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Cập nhật danh mục", description = "Cập nhật tên của một danh mục đã tồn tại.")
    public ResponseEntity<BlogCategoryResponse> updateBlogCategory(@PathVariable Long id, @Valid @RequestBody UpdateBlogCategoryRequest request) {
        return ResponseEntity.ok(blogService.updateBlogCategory(id, request));
    }

    @DeleteMapping("/admin/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Xóa danh mục", description = "Xóa một danh mục (chỉ khi không có bài viết nào thuộc danh mục đó).")
    public ResponseEntity<Void> deleteBlogCategory(@PathVariable Long id) {
        blogService.deleteBlogCategory(id);
        return ResponseEntity.noContent().build();
    }
}