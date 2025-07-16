package com.example.demo.repository;

import com.example.demo.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    Page<Notification> findByUser_IdAndIsReadFalse(Long userId, Pageable pageable);
    List<Notification> findByUser_IdAndIsReadFalse(Long userId);
    Page<Notification> findByUser_Id(Long userId, Pageable pageable);
}