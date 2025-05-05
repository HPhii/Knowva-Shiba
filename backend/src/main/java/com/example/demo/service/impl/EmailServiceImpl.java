package com.example.demo.service.impl;

import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.service.itf.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    @Override
    public void sendMail(EmailDetails emailDetails, String templateName, Map<String, Object> contextVariables) throws MessagingException {
        try {
            // Create the context for Thymeleaf template
            Context context = new Context();
            context.setVariables(contextVariables); // Set dynamic variables from the map

            // Process the template
            String templateContent = templateEngine.process(templateName, context);

            // Creating a simple mail message
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

            // Set the email details
            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetails.getReceiver().getEmail());
            mimeMessageHelper.setText(templateContent, true);
            mimeMessageHelper.setSubject(emailDetails.getSubject());

            // Send mail
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println("ERROR MAIL SENT: " + e.getMessage());
        }
    }
}

