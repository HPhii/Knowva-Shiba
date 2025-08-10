package com.example.demo.service.kafka;

import com.example.demo.model.io.dto.ImageUploadMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryProducerService {

    @Value("${spring.kafka.topic.cloudinary-upload}")
    private String cloudinaryUploadTopic;

    private final KafkaTemplate<String, ImageUploadMessage> kafkaTemplate;

    public void sendUploadRequest(MultipartFile file, Long userId) throws IOException {
        ImageUploadMessage message = new ImageUploadMessage(
                file.getBytes(),
                file.getOriginalFilename(),
                userId
        );
        log.info("Sending image upload request to Kafka topic '{}' for user {}", cloudinaryUploadTopic, userId);
        kafkaTemplate.send(cloudinaryUploadTopic, message);
    }
}