package com.example.demo.service.kafka;

import com.example.demo.model.io.dto.ImageUploadMessage;
import com.example.demo.service.intface.ICloudinaryService;
import com.example.demo.utils.ByteArrayMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryConsumerService {

    private final ICloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${spring.kafka.topic.cloudinary-upload}", groupId = "cloudinary_group", containerFactory = "imageUploadListenerContainerFactory")
    public void handleImageUpload(ImageUploadMessage message) {
        log.info("Received image upload request from Kafka for user {}", message.getUserId());
        try {
            // Chuyển đổi byte[] thành một đối tượng giống MultipartFile
            MultipartFile file = new ByteArrayMultipartFile(
                    message.getFileContent(),
                    message.getOriginalFilename(),
                    "application/octet-stream" // Kiểu MIME chung
            );

            // Gọi service để upload
            Map uploadResult = cloudinaryService.upload(file);
            String imageUrl = uploadResult.get("url").toString();

            log.info("Image uploaded successfully for user {}. URL: {}", message.getUserId(), imageUrl);

            // Gửi URL về cho client qua WebSocket
            // Topic sẽ là "/topic/cloudinary-upload/{userId}"
            String destination = "/topic/cloudinary-upload/" + message.getUserId();
            messagingTemplate.convertAndSend(destination, Map.of("url", imageUrl));

        } catch (Exception e) {
            log.error("Failed to upload image for user {}: {}", message.getUserId(), e.getMessage());
            // Gửi thông báo lỗi về cho client qua WebSocket
            String destination = "/topic/cloudinary-upload/" + message.getUserId();
            messagingTemplate.convertAndSend(destination, Map.of("error", "Upload failed: " + e.getMessage()));
            // Kafka Error Handler sẽ tự động đưa message này vào DLQ
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }
}
