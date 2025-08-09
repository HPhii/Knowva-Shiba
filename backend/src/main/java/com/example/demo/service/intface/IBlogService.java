package com.example.demo.service.intface;

import com.example.demo.model.enums.BlogPostStatus;
import com.example.demo.model.io.request.CreateBlogCategoryRequest;
import com.example.demo.model.io.request.CreateBlogPostRequest;
import com.example.demo.model.io.request.UpdateBlogCategoryRequest;
import com.example.demo.model.io.request.UpdateBlogPostRequest; // Thêm import
import com.example.demo.model.io.response.object.BlogCategoryResponse;
import com.example.demo.model.io.response.object.BlogPostResponse;
import com.example.demo.model.io.response.paged.PagedBlogPostResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IBlogService {
    // --- Blog Methods ---
    PagedBlogPostResponse getAllBlogPosts(BlogPostStatus status, Long categoryId, Long authorId, String keyword, Pageable pageable);
    BlogPostResponse getBlogPostById(Long id);
    BlogPostResponse getBlogPostBySlug(String slug);
    BlogPostResponse createBlogPost(CreateBlogPostRequest request, Long authorId);
    BlogPostResponse updateBlogPost(Long postId, UpdateBlogPostRequest request, Long userId);
    PagedBlogPostResponse getMyBlogPosts(Long authorId, BlogPostStatus status, Pageable pageable);
    void deleteBlogPost(Long id, Long userId);
    BlogPostResponse updateBlogPostStatus(Long id, BlogPostStatus status);

    // --- Category Methods ---
    List<BlogCategoryResponse> getAllBlogCategories();
    BlogCategoryResponse createBlogCategory(CreateBlogCategoryRequest request);
    BlogCategoryResponse updateBlogCategory(Long categoryId, UpdateBlogCategoryRequest request);
    void deleteBlogCategory(Long categoryId);
}