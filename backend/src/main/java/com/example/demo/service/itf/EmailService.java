package com.example.demo.service.itf;

import com.example.demo.model.io.response.object.EmailDetails;
import jakarta.mail.MessagingException;

import java.util.Map;

public interface EmailService {
    void sendMail(EmailDetails emailDetails, String templateName, Map<String, Object> contextVariables) throws MessagingException;
}
