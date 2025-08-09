package com.example.demo.model.io.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponse {
    private Long id;
    private Integer ratingValue;
    private String userName;
    private String userAvatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
