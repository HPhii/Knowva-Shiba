package com.example.demo.service.impl;

import com.cloudinary.utils.ObjectUtils; // <-- THÊM IMPORT NÀY
import com.example.demo.service.intface.ICloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncCloudinaryService {

    private final ICloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    // === THAY ĐỔI SIGNATURE CỦA PHƯƠNG THỨC ===
    @Async
    public void uploadImageAndNotify(byte[] fileBytes, String originalFilename, Long userId) {
        String destination = "/topic/cloudinary-upload/" + userId;
        try {
            log.info("Starting async image upload for user {}", userId);

            // Gọi phương thức upload mới (sẽ tạo ở bước 3)
            Map<String, Object> uploadResult = cloudinaryService.upload(
                    fileBytes,
                    ObjectUtils.asMap("resource_type", "auto", "public_id", originalFilename)
            );
            String imageUrl = uploadResult.get("url").toString();

            log.info("Image uploaded successfully for user {}. URL: {}", userId, imageUrl);

            messagingTemplate.convertAndSend(destination, Map.of("url", imageUrl));

        } catch (Exception e) {
            // Log cả stack trace để debug dễ hơn
            log.error("Failed to upload image for user {}: {}", userId, e.getMessage(), e);
            messagingTemplate.convertAndSend(destination, Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}