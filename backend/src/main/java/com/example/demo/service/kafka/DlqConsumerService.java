package com.example.demo.service.kafka;

import com.example.demo.model.io.dto.EmailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DlqConsumerService {

    @KafkaListener(topics = "${spring.kafka.topic.dlq}", groupId = "dlq_group")
    public void listenDlqEvents(EmailMessage failedMessage) {
        log.error("=============== FAILED EMAIL EVENT ===============");
        log.error("Received failed email message from DLQ: {}", failedMessage);
        // Tại đây bạn có thể:
        // 1. Gửi thông báo cho admin (qua Slack, Telegram,...)
        // 2. Lưu vào database để có giao diện quản lý và retry thủ công
        // 3. Phân tích lỗi và tìm ra nguyên nhân
        log.error("===================================================");
    }
}