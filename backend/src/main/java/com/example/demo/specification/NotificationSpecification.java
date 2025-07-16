package com.example.demo.specification;

import com.example.demo.model.entity.Notification;
import com.example.demo.model.enums.NotificationType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class NotificationSpecification {

    public static Specification<Notification> withUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Notification> withIsRead(Boolean isRead) {
        if (isRead == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isRead"), isRead);
    }

    public static Specification<Notification> withType(NotificationType type) {
        if (type == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Notification> withTimestampAfter(LocalDateTime startDate) {
        if (startDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startDate);
    }

    public static Specification<Notification> withTimestampBefore(LocalDateTime endDate) {
        if (endDate == null) {
            return null;
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endDate);
    }
}