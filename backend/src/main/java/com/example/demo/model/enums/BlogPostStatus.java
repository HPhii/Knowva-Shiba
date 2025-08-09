package com.example.demo.model.enums;

public enum BlogPostStatus {
    DRAFT,          // Bản nháp, chỉ tác giả thấy
    PENDING_APPROVAL, // Chờ duyệt, admin thấy
    PUBLISHED,      // Đã xuất bản, mọi người thấy
    REJECTED        // Bị từ chối
}