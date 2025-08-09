package com.example.demo.model.io.request;

import com.example.demo.model.enums.BlogPostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlogPostRequest {
    private String title;
    private String excerpt;
    private String content;
    private Long categoryId;
    private String imageUrl;

    private BlogPostStatus status;
}