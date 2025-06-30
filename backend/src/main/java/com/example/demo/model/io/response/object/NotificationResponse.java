package com.example.demo.model.io.response.object;

import com.example.demo.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private Long setId;
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;
}
