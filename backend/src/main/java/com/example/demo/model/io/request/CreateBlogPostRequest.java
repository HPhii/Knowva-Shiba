package com.example.demo.model.io.request;

import com.example.demo.model.enums.BlogPostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlogPostRequest {
    private String title;
    private String excerpt; // Optional - will auto-generate from content if not provided
    private String content;
    private Long categoryId;
    private String imageUrl;
    // readTime removed - will be auto-calculated from content
    private BlogPostStatus status;
}