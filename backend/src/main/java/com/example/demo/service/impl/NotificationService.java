package com.example.demo.service.impl;

import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.NotificationMapper;
import com.example.demo.model.entity.Account;
import com.example.demo.model.entity.Notification;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.NotificationType;
import com.example.demo.model.enums.Role;
import com.example.demo.model.io.response.object.NotificationResponse;
import com.example.demo.model.io.response.paged.PagedNotificationResponse;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IAccountService;
import com.example.demo.service.intface.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final NotificationMapper notificationMapper;
    private final IAccountService accountService;

    @Override
    public NotificationResponse createNotification(Long userId, NotificationType type, String message, Long setId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .setId(setId)
                .message(message)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        NotificationResponse notificationResponse = notificationMapper.toNotificationResponse(savedNotification);

        System.out.println("Sending notification DTO to /topic/notifications/" + userId);
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notificationResponse);

        return notificationResponse;
    }

    @Override
    public void createNotificationForAdmins(NotificationType type, String message, Long setId) {
        List<Account> adminAccounts = accountRepository.findByRole(Role.ADMIN);

        for (Account adminAccount : adminAccounts) {
            if (adminAccount.getUser() != null) {
                createNotification(adminAccount.getUser().getId(), type, message, setId);
            }
        }
    }

    @Override
    public PagedNotificationResponse getUnreadNotifications(Pageable pageable) {
        Long userId = accountService.getCurrentAccount().getUser().getId();

        Page<Notification> notificationsPage = notificationRepository.findByUser_IdAndIsReadFalse(userId, pageable);
        if (notificationsPage.isEmpty()) {
            throw new EntityNotFoundException("No unread notifications found for user with ID: " + userId);
        }

        List<NotificationResponse> notificationResponses = notificationMapper.toNotificationResponseList(notificationsPage.getContent());

        PagedNotificationResponse response = new PagedNotificationResponse(
                notificationResponses,
                notificationsPage.getTotalElements(),
                notificationsPage.getTotalPages(),
                notificationsPage.getNumber()
        );

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, response);
        return response;
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUser_IdAndIsReadFalse(userId);
        if (notifications.isEmpty()) {
            throw new EntityNotFoundException("No unread notifications found for user with ID: " + userId);
        }

        notifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(notifications);

        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notifications);
    }

    // get all notifications of current user
    @Override
    public PagedNotificationResponse getAllNotifications(Pageable pageable) {
        Long userId = accountService.getCurrentAccount().getUser().getId();

        Page<Notification> notificationsPage = notificationRepository.findByUser_Id(userId, pageable);
        if (notificationsPage.isEmpty()) {
            throw new EntityNotFoundException("No notifications found for user with ID: " + userId);
        }

        List<NotificationResponse> notificationResponses = notificationMapper.toNotificationResponseList(notificationsPage.getContent());

        return new PagedNotificationResponse(
                notificationResponses,
                notificationsPage.getTotalElements(),
                notificationsPage.getTotalPages(),
                notificationsPage.getNumber()
        );
    }

    @Override
    public void createSystemNotification(NotificationType type, String message, Long setId) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            createNotification(user.getId(), type, message, setId);
        }
    }
}
