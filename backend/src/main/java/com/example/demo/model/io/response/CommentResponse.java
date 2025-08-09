package com.example.demo.model.io.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String content;
    private String imageUrl; // Thêm trường để trả về ảnh đính kèm
    private String userName;
    private String userAvatarUrl;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentCommentId;
    private List<CommentResponse> replies;
    private Integer replyCount;
}
