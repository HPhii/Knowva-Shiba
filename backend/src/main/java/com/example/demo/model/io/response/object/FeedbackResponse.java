package com.example.demo.model.io.response.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private Long userId;
    private String username;
    private Integer rating;
    private String message;
    private LocalDateTime createdAt;
}