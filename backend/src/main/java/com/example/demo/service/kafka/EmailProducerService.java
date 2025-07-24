package com.example.demo.service.kafka;

import com.example.demo.model.io.dto.EmailMessage;
import com.example.demo.model.io.response.object.EmailDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProducerService {

    private final KafkaTemplate<String, EmailMessage> kafkaTemplate;

    public void sendEmailEvent(EmailDetails emailDetails, String templateName, Map<String, Object> contextVariables) {
        EmailMessage emailMessage = new EmailMessage(emailDetails, templateName, contextVariables);
        log.info("Sending email event to Kafka: {}", emailMessage);
        kafkaTemplate.send("email_events", emailMessage);
    }
}
