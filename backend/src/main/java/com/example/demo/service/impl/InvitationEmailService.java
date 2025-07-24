package com.example.demo.service.impl;

import com.example.demo.model.entity.User;
import com.example.demo.model.enums.Permission;
import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.service.intface.IInvitationEmailService;
import com.example.demo.service.kafka.EmailProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvitationEmailService implements IInvitationEmailService {

    private final EmailProducerService emailProducerService;

    @Override
    public void sendInvitationEmail(User inviter, User invitedUser, Long setId, String setName, String setType, Permission permission) {
        String subject = "Bạn có lời mời học tập mới từ " + inviter.getAccount().getUsername();
        String templateName = "invite.html";

        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("invitedUserName", invitedUser.getFullName() != null ? invitedUser.getFullName() : invitedUser.getAccount().getUsername());
        contextVariables.put("inviterName", inviter.getFullName() != null ? inviter.getFullName() : inviter.getAccount().getUsername());
        contextVariables.put("setType", setType); // "Flashcard" hoặc "Quiz"
        contextVariables.put("setName", setName);
        contextVariables.put("permission", permission == Permission.EDIT ? "Chỉnh sửa" : "Xem");
        contextVariables.put("setId", setId);

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(invitedUser.getAccount());
        emailDetails.setSubject(subject);

        emailProducerService.sendEmailEvent(emailDetails, templateName, contextVariables);
    }
}