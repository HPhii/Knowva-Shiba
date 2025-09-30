package com.example.demo.service.intface;

import com.example.demo.model.entity.ActivityLog;
import com.example.demo.model.entity.User;
import com.example.demo.model.enums.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IActivityLogService {
    void logActivity(User user, ActivityType type, String description, Long entityId);
    Page<ActivityLog> getActivitiesForUser(Long userId, Pageable pageable);
}
