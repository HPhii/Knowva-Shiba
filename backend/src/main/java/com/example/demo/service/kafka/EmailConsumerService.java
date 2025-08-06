package com.example.demo.service.kafka;


import com.example.demo.model.io.dto.EmailMessage;
import com.example.demo.service.intface.IEmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumerService {

    private final IEmailService emailService;

    @KafkaListener(topics = "email_events", groupId = "email_group", containerFactory = "kafkaListenerContainerFactory")
    public void listenEmailEvents(EmailMessage emailMessage) {
        log.info("Received email event from Kafka: {}", emailMessage);
        try {
            emailService.sendMail(emailMessage.getEmailDetails(), emailMessage.getTemplateName(), emailMessage.getContextVariables());
            log.info("Email sent successfully to: {}", emailMessage.getEmailDetails().getReceiver().getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send email to: {} due to {}", emailMessage.getEmailDetails().getReceiver().getEmail(), e.getMessage());
            // Xử lý lỗi gửi mail, ví dụ: retry hoặc ghi log chi tiết hơn
        }
    }
}
