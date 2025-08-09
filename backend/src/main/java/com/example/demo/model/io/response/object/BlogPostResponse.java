package com.example.demo.model.io.response.object;

import com.example.demo.model.enums.BlogPostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostResponse {
    private Long id;
    private String title;
    private String slug;
    private String excerpt;
    private String content;
    private String authorName;
    private LocalDateTime publishedAt;
    private BlogPostStatus status;
    private String categoryName;
    private String imageUrl;
    private String readTime;
}