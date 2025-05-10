package com.example.demo.service.intface;

import com.example.demo.model.io.response.object.EmailDetails;
import jakarta.mail.MessagingException;

import java.util.Map;

public interface IEmailService {
    void sendMail(EmailDetails emailDetails, String templateName, Map<String, Object> contextVariables) throws MessagingException;
}

