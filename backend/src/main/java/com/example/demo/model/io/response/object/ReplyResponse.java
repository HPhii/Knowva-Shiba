package com.example.demo.model.io.response.object;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReplyResponse {
    private Long id;
    private String message;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private List<ReplyResponse> children;
}