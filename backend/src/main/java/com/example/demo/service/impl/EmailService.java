package com.example.demo.service.impl;

import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.service.intface.IEmailService;
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
public class EmailService implements IEmailService {
    private final TemplateEngine templateEngine;
    private final JavaMailSender mailSender;

    @Override
    public void sendMail(EmailDetails emailDetails, String templateName, Map<String, Object> contextVariables) throws MessagingException {
        try {
            Context context = new Context();
            context.setVariables(contextVariables);
            context.setVariable("subject", emailDetails.getSubject());
            context.setVariable("template", "email/" + templateName);

            String htmlContent = templateEngine.process("email/base", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            mimeMessageHelper.setFrom("admin@gmail.com");
            mimeMessageHelper.setTo(emailDetails.getReceiver().getEmail());
            mimeMessageHelper.setSubject(emailDetails.getSubject());
            mimeMessageHelper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            System.out.println("ERROR MAIL SENT: " + e.getMessage());
            throw e;
        }
    }
}
