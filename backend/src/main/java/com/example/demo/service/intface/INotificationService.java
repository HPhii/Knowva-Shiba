package com.example.demo.service.intface;

import com.example.demo.model.enums.NotificationType;
import com.example.demo.model.io.response.object.NotificationResponse;
import com.example.demo.model.io.response.paged.PagedNotificationResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface INotificationService {
    NotificationResponse createNotification(Long userId, NotificationType type, String message, Long setId);
    PagedNotificationResponse getNotifications(Boolean isRead, NotificationType type, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    void markAsRead(Long notificationId);
    void markAllAsRead(Long userId);
    void createSystemNotification(NotificationType type, String message, Long setId);
    void createNotificationForAdmins(NotificationType type, String message, Long setId);
}