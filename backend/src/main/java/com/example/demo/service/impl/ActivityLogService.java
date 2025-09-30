package com.example.demo.service.impl;

import com.example.demo.model.entity.ActivityLog;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.ActivityType;
import com.example.demo.repository.ActivityLogRepository;
import com.example.demo.service.intface.IActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityLogService implements IActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Async
    @Override
    public void logActivity(User user, ActivityType type, String description, Long entityId) {
        ActivityLog log = ActivityLog.builder()
                .user(user)
                .type(type)
                .description(description)
                .entityId(entityId)
                .build();
        activityLogRepository.save(log);
    }

    @Override
    public Page<ActivityLog> getActivitiesForUser(Long userId, Pageable pageable) {
        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
}
